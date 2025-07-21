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

## 第1段階: Java 17 + Gradle移行

### 1.1 ビルド設定更新
- [x] `build.gradle.kts`のJavaバージョンを17に更新
  - `java.sourceCompatibility = JavaVersion.VERSION_17`
  - `jvmTarget = "17"`
- [x] Jibプラグイン設定の更新
  - ベースイメージを`eclipse-temurin:17`に変更

### 1.2 コード修正
- [ ] Java 8→21で非推奨になったAPIの調査
- [ ] 必要に応じてコード修正

### 1.3 動作確認
- [ ] ローカルでのビルド成功確認（`./gradlew clean build -x test`）
- [ ] Spring Boot Testの起動確認のみ実行
- [ ] アプリケーションの起動確認

### 1.4 コミット
- [ ] "Java 8から17へアップグレード"としてコミット

## 第2段階: Spring Boot 2.7.x + Kotlin 1.8移行

### 2.1 依存関係更新
- [ ] Spring Bootバージョンを2.7.18に更新
- [ ] Spring Dependency Managementプラグインの更新
- [ ] Kotlinバージョンを1.8.22に更新

### 2.2 設定ファイル更新
- [ ] `application.yml`の非推奨プロパティ確認と修正
- [ ] `application-production.yml`の確認

### 2.3 コード修正
- [ ] Spring Boot 2.7での非推奨API確認
- [ ] 警告の解消

### 2.4 テスト設定とテスト
- [ ] Firebase関連のモック設定追加
- [ ] テスト用プロファイルの設定（H2データベース）
- [ ] Spring Boot Testの起動確認
- [ ] 外部依存を除く単体テストの実行

### 2.5 コミット
- [ ] "Spring Boot 2.3.0から2.7.18、Kotlin 1.4.20から1.8.22へ移行"としてコミット

## 第3段階: Spring Boot 3.0.x + Kotlin 1.9 + Java 21移行

### 3.1 Jakarta EE移行準備
- [ ] 移行ツールのセットアップ（必要に応じて）
- [ ] 影響範囲の最終確認

### 3.2 依存関係更新
- [ ] Java 17から21へアップグレード
  - [ ] `java.sourceCompatibility = JavaVersion.VERSION_21`
  - [ ] `jvmTarget = "21"`
  - [ ] Dockerイメージを`eclipse-temurin:21`に変更
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
- [ ] Firebaseモック設定の更新（Jakarta EE対応）
- [ ] Spring Boot Testの起動確認
- [ ] 外部依存を除く単体テストの修正と実行
- [ ] H2データベースでの統合テスト確認

### 3.8 コミット
- [ ] "Spring Boot 2.7.18から3.0.13、Kotlin 1.8.22から1.9.25、Java 17から21へ移行（Jakarta EE対応）"としてコミット

## 第4段階: Spring Boot 3.4移行

### 4.1 最終更新
- [ ] Spring Bootバージョンを3.4.0に更新
- [ ] 関連プラグインの最終更新

### 4.2 新機能対応
- [ ] Spring Boot 3.4の新機能確認
- [ ] 必要に応じて最適化

### 4.3 最終テスト
- [ ] Spring Boot Testの起動確認
- [ ] モック化されたテストスイートの実行
- [ ] アプリケーション起動時間の確認

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
- [ ] Spring Boot Testでのアプリケーション起動確認
- [ ] 依存関係の脆弱性チェック（`./gradlew dependencyCheckAnalyze`）
- [ ] ローカル環境での手動起動確認

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

### テスト戦略の補足
Firebase関連機能とテストについて：
- **実環境テスト**: 手動で起動確認のみ
- **自動テスト**: 外部依存は全てモック化
- **テスト目標**: Spring Boot Testの起動成功を主眼とする

### 推定作業時間
- 第0段階: 1時間
- 第1段階: 2-3時間
- 第2段階: 4-5時間（テストモック設定含む）
- 第3段階: 8-10時間（最も複雑）
- 第4段階: 2-3時間
- 第5段階: 2-3時間

合計: 約20-25時間