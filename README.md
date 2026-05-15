# FIREWOLF

人狼ゲーム FIREWOLF の monorepo。

## 構成

```
.
├── frontend/   # Nuxt 4 / pnpm 製のWebフロントエンド (旧 firewolf-ui)
├── backend/    # Kotlin / Gradle / Spring Boot 製のAPIサーバ (旧 firewolf-api)
└── e2e/        # E2Eテスト (Playwright 想定、未整備)
```

旧リポジトリの履歴は `git-filter-repo` で各サブディレクトリ配下に保持されています。
個別ファイルの履歴は `git log --follow frontend/...` のように `--follow` を付けると追跡できます。

## 開発

### frontend
```bash
cd frontend
pnpm install
pnpm dev
```

### backend
```bash
cd backend
./gradlew bootRun
```

## 旧リポジトリ

- https://github.com/h-orito/firewolf-ui (frontend のオリジナル / archive 予定)
- https://github.com/h-orito/firewolf-api (backend のオリジナル / archive 予定)
