# 役職追加手順

人狼ゲームに新しい役職を追加します。

## 入力情報

ユーザーから以下の情報を確認してください:
- 役職コード（英語大文字、例: BIGWOLF）
- 役職名（日本語、例: 大狼）
- 役職略称（1文字、例: 大）
- 陣営コード（VILLAGER / WEREWOLF / FOX / LOVERS）
- 表示順（disp_order、同陣営内での並び順）
- 役職説明文
- 所属する分類グループ（classificationDefinitionMap.dfpropのgroupingMapに定義されているグループ）

## 分類グループ一覧

以下のグループが利用可能です。役職の特性に応じて適切なグループに追加してください:

### 発言系
- `viewableWerewolfSay`: 囁きを見られる（人狼系+C国狂人）
- `availableWerewolfSay`: 囁き可能（人狼系+C国狂人）
- `viewableSympathizeSay`: 共鳴発言を見られる
- `availableSympathizeSay`: 共鳴発言可能

### 襲撃系
- `notSelectableAttack`: 襲撃対象に選べない（人狼系）
- `hasAttackAbility`: 襲撃能力を持つ
- `noDeadByAttack`: 襲撃耐性を持つ（妖狐）

### 占い・霊能系
- `divineResultWolf`: 占い結果が人狼になる
- `psychicResultWolf`: 霊能結果が人狼になる
- `hasDivineAbility`: 占い能力を持つ
- `hasWiseDivineAbility`: 役職占い能力を持つ
- `hasPsychicAbility`: 霊能能力を持つ
- `hasGuruPsychicAbility`: 役職霊能能力を持つ
- `hasAutopsyAbility`: 検死能力を持つ
- `hasWiseWolfAbility`: 襲撃役職占い能力を持つ
- `deadByDivine`: 占いにより死亡する（妖狐）
- `counterDeadByDivine`: 占いにより占った側が死亡する（呪狼）

### 護衛系
- `hasGuardAbility`: 護衛能力を持つ
- `hasWandererGuardAbility`: 風来護衛能力を持つ

### 勝敗判定系
- `countWolf`: 勝敗判定時狼にカウントする
- `noCount`: 勝敗判定時に人間としてカウントしない（妖狐）

### 認知系
- `recognizableEachMason`: 共有者としてお互いに認知できる
- `recognizableEachSympathizer`: 共鳴者としてお互いに認知できる
- `recognizableWolf`: 人狼を認知できる（狂信者）
- `recognizableFox`: 妖狐を認知できる

### その他
- `someoneSkill`: おまかせ系
- `hasBakeryAbility`: パン焼き能力を持つ
- `hasLuckyAbility`: 強運能力を持つ
- `forceDoubleSuicide`: 死亡時道連れにする（猫又）

## 実装手順

以下の手順を順番に実施してください:

### 1. TSVファイルに役職を追加
`dbflute_firewolfdb/playsql/data/common/tsv/UTF-8/04-SKILL.tsv` にタブ区切りで行を追加します。
- disp_orderが既存の役職と衝突する場合、既存のdisp_orderをずらしてください。
- フォーマット: `{skill_code}\t{skill_name}\t{skill_short_name}\t{camp_code}\t{disp_order}\t{description}`

### 2. classificationDefinitionMap.dfpropを編集
`dbflute_firewolfdb/dfprop/classificationDefinitionMap.dfprop` のSkill定義内のgroupingMapで、該当する分類グループのelementListに役職名（日本語）を追加します。

### 3. DBFlute自動生成を実行
```bash
cd dbflute_firewolfdb && echo y | sh manage.sh 1
```
これにより `CDef.java` などのORM型定義が自動生成されます。

### 4. Skill.ktに能力マッピングを追加
`src/main/kotlin/com/ort/firewolf/domain/model/skill/Skill.kt` の以下2つのマップに新しい役職を追加します:
- `skillAbilityTypeListMap`: 実際の能力（CDef.AbilityType.襲撃、CDef.AbilityType.占い など）
- `skillAbilityListMapForManual`: 説明書用の能力表示

能力を持たない役職（村人、狂人、聖痕者など）の場合はマップへの追加不要です。

### 5. 特殊能力の実装（必要な場合のみ）
新しい分類グループが必要な特殊能力がある場合:
1. `classificationDefinitionMap.dfprop` に新しいグループを追加
2. DBFlute自動生成を再実行
3. 対応するDomainServiceを実装（`domain/service/ability/` 配下）
4. `ProgressDomainService.kt` の日次処理に組み込み

既存の分類グループで表現できる能力の場合は、グループへの追加だけで動作します。

### 6. ビルド確認
```bash
./gradlew build
```

## 注意事項
- `com.ort.dbflute` 配下は自動生成ファイルのため直接編集しないでください
- 多くの動作は分類グループ（CDef）ベースで自動的に決定されるため、グループへの追加だけで動作する場合が多いです
- 新しい能力種別を追加する場合は、ABILITY_TYPEテーブルへの追加も必要です
