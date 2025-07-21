# Spring Boot 3.4 移行設計書

## 概要
Firewolf APIをSpring Boot 2.3.0からSpring Boot 3.4へ移行する設計書です。

## 現状分析

### 現在のバージョン
- Spring Boot: 2.3.0.RELEASE
- Kotlin: 1.4.20
- Java: 8
- Gradle: 6.4.1

### 主要な技術スタック
- DBFlute 1.2.1（ORM）
- Firebase Admin SDK 6.8.1（認証）
- MySQL Connector 8.0.25
- JWT 0.10.7

## 移行方針

### 1. 段階的移行アプローチ
大規模な破壊的変更を避けるため、以下の段階で移行を実施します：

1. **第1段階**: Java 17 + Gradle 8.14.3への移行
2. **第2段階**: Spring Boot 2.7.x + Kotlin 1.8.22への移行
3. **第3段階**: Spring Boot 3.0.x + Kotlin 1.9.25 + Java 21への移行
4. **第4段階**: Spring Boot 3.4.xへの最終移行

### 2. 主要な変更点

#### 2.1 Javaバージョン
- **変更前**: Java 8
- **変更後**: Java 21（最新LTS版）
- **段階的更新**:
  - 第1段階: Java 17（互換性確保）
  - 第3段階: Java 21（最新LTS、Virtual Threads対応）
- **影響範囲**: 
  - Dockerイメージの変更（openjdk:8 → eclipse-temurin:21）
  - build.gradle.ktsのjvmTarget変更
  - Virtual Threadsなど新機能の活用が可能

#### 2.2 Kotlinバージョン
- **変更前**: 1.4.20
- **変更後**: 1.9.25（最新安定版）
- **段階的更新**: 
  - 第2段階: 1.8.22（Spring Boot 2.7対応）
  - 第3段階: 1.9.25（Spring Boot 3.x対応、Java 21サポート）
- **影響範囲**: 
  - コンパイラ設定の更新
  - 一部の非推奨APIの修正

#### 2.3 Gradleバージョン
- **変更前**: 6.4.1
- **変更後**: 8.14.3（2025年7月時点の最新安定版）
- **影響範囲**: 
  - gradle-wrapper.propertiesの更新
  - ビルドスクリプトの構文更新
  - Java 24までサポート（将来の拡張性確保）
  - パフォーマンスとメモリ使用量の改善

#### 2.4 Jakarta EE移行
- **主な変更**: javax.*パッケージ → jakarta.*パッケージ
- **影響範囲**:
  - `javax.validation` → `jakarta.validation`
  - `javax.servlet` → `jakarta.servlet`
  - `javax.persistence` → `jakarta.persistence`

#### 2.5 Spring Security設定
- **変更前**: `WebSecurityConfigurerAdapter`を継承
- **変更後**: `SecurityFilterChain` Beanを定義
- **影響範囲**: SecurityConfigクラスの完全な書き換え

### 3. 依存関係の更新計画

#### 必須更新
```kotlin
// Spring Boot
id("org.springframework.boot") version "3.4.0"
id("io.spring.dependency-management") version "1.1.6"

// Kotlin
kotlin("jvm") version "1.9.25"
kotlin("plugin.spring") version "1.9.25"

// Jib（Dockerイメージビルド）
id("com.google.cloud.tools.jib") version "3.4.4"
```

#### 依存ライブラリの更新
```kotlin
// DBFlute - Jakarta EE対応バージョンを確認・更新
implementation("org.dbflute:dbflute-runtime:1.2.8")

// JWT - 最新版へ更新
implementation("io.jsonwebtoken:jjwt-api:0.12.6")
implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

// Firebase Admin - 最新版へ更新
implementation("com.google.firebase:firebase-admin:9.3.0")

// MySQL Connector - 既に8.x系なので維持
implementation("mysql:mysql-connector-java:8.0.33")

// Caffeine - 最新版へ更新
implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

// Test
testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
```

### 4. コード変更の影響範囲

#### 4.1 パッケージ名の変更
- すべての`javax.*`インポートを`jakarta.*`に変更
- 主な対象ファイル：
  - Validationアノテーション使用箇所（`com.ort.firewolf`パッケージ内）
  - Filterクラス
  - **注意**: `com.ort.dbflute`パッケージは自動生成のため手動変更不要

#### 4.2 Spring Security設定
- `SecurityConfig.kt`の完全な書き換え
- 認証・認可設定の新方式への移行

#### 4.3 プロパティ設定
- `application.yml`の一部プロパティ名変更
- MySQLドライバークラス名の更新

### 5. リスクと対策

#### リスク1: DBFluteのJakarta EE非対応
- **対策**: DBFluteの最新版で対応状況を確認。DBFluteクライアントの再生成で対応

#### リスク2: 外部連携ライブラリの互換性
- **対策**: Twitter/Slack APIクライアントの動作確認とバージョン更新

#### リスク3: テストコードの大幅な修正
- **対策**: 段階的移行で影響を最小化

### 6. テスト計画

#### 6.1 テスト対象の制限
Firebase関連機能は外部依存が多いため、以下の戦略を採用：
- **Firebase認証**: モック化して単体テストのみ
- **外部API連携**: モック化（Twitter/Slack/Discord）
- **データベース**: H2インメモリDBを使用した統合テスト

#### 6.2 テスト範囲
1. **Spring Boot Testの起動確認**: アプリケーションコンテキストの正常起動
2. **単体テスト**: ビジネスロジックの動作確認（外部依存はモック）
3. **統合テスト**: データベース操作の確認（H2使用）
4. **設定テスト**: application.ymlの設定値読み込み確認

### 7. 実装順序

1. **環境準備**
   - Java 21のインストール
   - Gradle 8.14.3へのアップグレード

2. **第1段階の実装**
   - build.gradle.ktsの更新（Java 21対応）
   - Dockerfileの更新

3. **第2段階の実装**
   - Spring Boot 2.7.xへの移行
   - 非推奨APIの修正

4. **第3段階の実装**
   - Spring Boot 3.0.xへの移行
   - Jakarta EE移行
   - Spring Security設定の更新

5. **第4段階の実装**
   - Spring Boot 3.4への最終移行
   - 最適化と調整

### 8. ロールバック計画

各段階でGitのブランチを作成し、問題が発生した場合は前の段階に戻れるようにする。

### 9. 移行完了の判定基準

- 全ての自動テストが成功
- アプリケーションの正常起動
- 主要機能の動作確認完了
- パフォーマンスの劣化がないこと

### 10. Gradleアップグレード手順

Gradle Wrapperを使用している場合、以下のコマンドで簡単にアップグレード可能：

```bash
./gradlew wrapper --gradle-version=8.14.3 --distribution-type=bin
```

これにより、gradle-wrapper.propertiesが自動的に更新されます。