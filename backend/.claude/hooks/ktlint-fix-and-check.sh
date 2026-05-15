#!/usr/bin/env bash
set -euo pipefail

# PostToolUse hook: Edit/Write後、変更対象のソースセットに対し
# 1) ktlintFormat で自動修正
# 2) ktlintCheck で残違反を検出し additionalContext として返す
# 同一ファイルへの連続失敗が MAX_RETRIES 回に達したらスキップ。

MAX_RETRIES=3

INPUT=$(cat)

FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')
if [[ -z "$FILE_PATH" ]]; then
  exit 0
fi

# .kt / .kts のみ対象
if [[ "$FILE_PATH" != *.kt && "$FILE_PATH" != *.kts ]]; then
  exit 0
fi

PROJECT_ROOT=$(echo "$INPUT" | jq -r '.cwd // empty')
if [[ -z "$PROJECT_ROOT" ]]; then
  exit 0
fi

REL_PATH="${FILE_PATH#"$PROJECT_ROOT"/}"

# DBFlute 自動生成ディレクトリは対象外
if [[ "$REL_PATH" == *"/com/ort/dbflute/"* ]]; then
  exit 0
fi

# build/ や generated 配下は対象外
if [[ "$REL_PATH" == build/* || "$REL_PATH" == */build/* ]]; then
  exit 0
fi
if [[ "$REL_PATH" == *"/generated/"* ]]; then
  exit 0
fi

# ステートディレクトリ（.context/ は .gitignore 済み）
STATE_DIR="$PROJECT_ROOT/.context/ktlint-hook"
mkdir -p "$STATE_DIR"

# ファイルパスをハッシュしてステートファイル名にする
if command -v md5sum >/dev/null 2>&1; then
  FILE_HASH=$(echo -n "$REL_PATH" | md5sum | cut -d' ' -f1)
else
  FILE_HASH=$(echo -n "$REL_PATH" | md5 -q)
fi
STATE_FILE="$STATE_DIR/$FILE_HASH"

FAIL_COUNT=0
if [[ -f "$STATE_FILE" ]]; then
  FAIL_COUNT=$(cat "$STATE_FILE")
fi

# --- 単一モジュール用に Gradle タスクを特定 ---
# パターン:
#   src/main/...     -> ktlintMainSourceSet*
#   src/test/...     -> ktlintTestSourceSet*
#   *.kts (root)     -> ktlintKotlinScript*
FORMAT_TASK=""
CHECK_TASK=""

if [[ "$REL_PATH" == src/main/* ]]; then
  FORMAT_TASK="ktlintMainSourceSetFormat"
  CHECK_TASK="ktlintMainSourceSetCheck"
elif [[ "$REL_PATH" == src/test/* ]]; then
  FORMAT_TASK="ktlintTestSourceSetFormat"
  CHECK_TASK="ktlintTestSourceSetCheck"
elif [[ "$FILE_PATH" == *.kts ]]; then
  FORMAT_TASK="ktlintKotlinScriptFormat"
  CHECK_TASK="ktlintKotlinScriptCheck"
else
  rm -f "$STATE_FILE"
  exit 0
fi

cd "$PROJECT_ROOT"

# --- リトライ上限到達後は Check のみ1回試行 ---
if [[ "$FAIL_COUNT" -ge "$MAX_RETRIES" ]]; then
  if ./gradlew "$CHECK_TASK" > /dev/null 2>&1; then
    rm -f "$STATE_FILE"
  else
    jq -n '{"hookSpecificOutput": {"hookEventName": "PostToolUse", "additionalContext": "ktlint check skipped (max retries reached). Run ./gradlew ktlintCheck manually."}}'
  fi
  exit 0
fi

# --- 1. ktlintFormat で自動修正 ---
./gradlew "$FORMAT_TASK" > /dev/null 2>&1 || true

# --- 2. ktlintCheck で残違反を検出 ---
./gradlew "$CHECK_TASK" > /dev/null 2>&1 && CHECK_EXIT=0 || CHECK_EXIT=$?

if [[ "$CHECK_EXIT" -eq 0 ]]; then
  rm -f "$STATE_FILE"
  exit 0
fi

# --- レポートから違反内容を取得 ---
REPORT_DIR="build/reports/ktlint/${CHECK_TASK}"
REPORT_FILE=$(find "$REPORT_DIR" -name '*.txt' 2>/dev/null | head -1)

VIOLATIONS=""
if [[ -n "$REPORT_FILE" && -f "$REPORT_FILE" ]]; then
  VIOLATIONS=$(sed 's/\x1b\[[0-9;]*m//g' "$REPORT_FILE" | grep -v '^$' | grep -v '^Summary' | head -30)
fi

if [[ -z "$VIOLATIONS" ]]; then
  VIOLATIONS="ktlintCheck failed for $CHECK_TASK but no detailed report found. Run ./gradlew $CHECK_TASK to see details."
fi

echo $((FAIL_COUNT + 1)) > "$STATE_FILE"
REMAINING=$((MAX_RETRIES - FAIL_COUNT - 1))

jq -n \
  --arg violations "$VIOLATIONS" \
  --argjson remaining "$REMAINING" \
  '{
    hookSpecificOutput: {
      hookEventName: "PostToolUse",
      additionalContext: ("ktlint violations remain after ktlintFormat. Please fix them manually.\nRemaining auto-check retries: " + ($remaining | tostring) + "\n\n" + $violations)
    }
  }'
