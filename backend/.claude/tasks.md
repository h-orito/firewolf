# Spring Boot 3.4 移行タスク一覧

## 概要
Spring Boot 2.3.0から3.4への段階的移行に必要なタスクを管理します。

## タスクステータス
- [ ] 未着手
- [x] 完了
- [!] ブロッカーあり

## 第0段階: 準備作業

### 0.1 環境準備
- [ ] Java 21のインストールと環境変数設定
- [ ] 開発環境でのJava 21動作確認
- [ ] IDEのJava 21対応設定


### 0.2 Gradleアップグレード
- [ ] Gradle Wrapperを8.14.3へアップグレード
  ```bash
  ./gradlew wrapper --gradle-version=8.14.3 --distribution-type=bin
  ```
- [ ] Gradleビルドの動作確認

## 第1段階: Java 21移行

### 1.1 ビルド設定更新
- [ ] `build.gradle.kts`のJavaバージョンを21に更新
  - `java.sourceCompatibility = JavaVersion.VERSION_21`
  - `jvmTarget = "21"`
- [ ] Jibプラグイン設定の更新
  - ベースイメージを`eclipse-temurin:21`に変更

### 1.2 コード修正
- [ ] Java 8→21で非推奨になったAPIの調査
- [ ] 必要に応じてコード修正

### 1.3 動作確認
- [ ] ローカルでのビルド成功確認
- [ ] 全テストの実行と成功確認
- [ ] アプリケーションの起動確認

### 1.4 コミット
- [ ] "Java 8から21へアップグレード"としてコミット

## 第2段階: Spring Boot 2.7.x移行

### 2.1 依存関係更新
- [ ] Spring Bootバージョンを2.7.18に更新
- [ ] Spring Dependency Managementプラグインの更新

### 2.2 設定ファイル更新
- [ ] `application.yml`の非推奨プロパティ確認と修正
- [ ] `application-production.yml`の確認

### 2.3 コード修正
- [ ] Spring Boot 2.7での非推奨API確認
- [ ] 警告の解消

### 2.4 テスト
- [ ] 単体テストの実行
- [ ] 統合テストの実行
- [ ] 手動での主要機能確認

### 2.5 コミット
- [ ] "Spring Boot 2.3.0から2.7.18へ移行"としてコミット

## 第3段階: Spring Boot 3.0.x移行

### 3.1 Jakarta EE移行準備
- [ ] 移行ツールのセットアップ（必要に応じて）
- [ ] 影響範囲の最終確認

### 3.2 依存関係更新
- [ ] Spring Bootバージョンを3.0.13に更新
- [ ] Kotlinバージョンを1.9.25に更新
- [ ] その他の依存関係更新
  - [ ] DBFlute Jakarta EE対応版
  - [ ] JWT最新版
  - [ ] Firebase Admin SDK最新版
  - [ ] Caffeine最新版
  - [ ] Mockito Kotlin最新版

### 3.3 パッケージ移行
- [ ] `javax.*` → `jakarta.*`への一括置換（`com.ort.firewolf`パッケージ内のみ）
  - [ ] Validationアノテーション
  - [ ] Servletフィルター
  - [ ] その他のjavax使用箇所
  ※ `com.ort.dbflute`パッケージは自動生成のため変更不要

### 3.4 Spring Security設定更新
- [ ] `SecurityConfig.kt`の完全書き換え
  - [ ] `WebSecurityConfigurerAdapter`の削除
  - [ ] `SecurityFilterChain` Beanの実装
- [ ] CORS設定の更新
- [ ] 認証・認可ルールの再実装

### 3.5 プロパティ更新
- [ ] MySQLドライバークラス名の更新
- [ ] その他のプロパティ名変更対応

### 3.6 DBFlute対応
- [ ] DBFluteのJakarta EE対応バージョン確認
- [ ] DBFluteクライアントの再生成
  ```bash
  cd dbflute_firewolfdb
  sh manage.sh regenerate
  ```

### 3.7 テスト
- [ ] コンパイルエラーの解消
- [ ] 単体テストの修正と実行
- [ ] 統合テストの修正と実行
- [ ] セキュリティ機能の動作確認

### 3.8 コミット
- [ ] "Spring Boot 2.7.18から3.0.13へ移行（Jakarta EE対応）"としてコミット

## 第4段階: Spring Boot 3.4移行

### 4.1 最終更新
- [ ] Spring Bootバージョンを3.4.0に更新
- [ ] 関連プラグインの最終更新

### 4.2 新機能対応
- [ ] Spring Boot 3.4の新機能確認
- [ ] 必要に応じて最適化

### 4.3 最終テスト
- [ ] 全テストスイートの実行
- [ ] パフォーマンステスト
- [ ] 負荷テスト

### 4.4 コミット
- [ ] "Spring Boot 3.0.13から3.4.0へ最終移行"としてコミット

## 第5段階: 仕上げ

### 5.1 ドキュメント更新
- [ ] README.mdの更新（必要なJavaバージョンなど）
- [ ] CLAUDE.mdの更新
- [ ] デプロイ手順の確認と更新

### 5.2 CI/CD更新
- [ ] GitHub ActionsのJavaバージョン更新
- [ ] Dockerfileの最適化

### 5.3 最終確認
- [ ] 全機能の動作確認
- [ ] セキュリティスキャン
- [ ] 依存関係の脆弱性チェック

### 5.4 コミット
- [ ] "ドキュメントとCI/CD設定を更新"としてコミット

## 注意事項

### ブロッカーになりうる項目
1. **DBFlute Jakarta EE対応**
   - DBFluteクライアントの再生成で対応可能
   - `com.ort.dbflute`パッケージは自動生成のため手動変更不要
   
2. **外部API連携ライブラリ**
   - Twitter/Slack/Discord連携の動作確認が必須

3. **Firebase Admin SDK**
   - 認証機能の完全な動作確認が必要

### ロールバック手順
各段階完了後にタグを作成し、問題発生時は以下で戻す：
```bash
git checkout -b rollback/{段階名} {タグ名}
```

### 推定作業時間
- 第0段階: 1時間
- 第1段階: 2-3時間
- 第2段階: 3-4時間
- 第3段階: 8-10時間（最も複雑）
- 第4段階: 2-3時間
- 第5段階: 2-3時間

合計: 約20-25時間