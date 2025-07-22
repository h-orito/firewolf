# 設計ドキュメント

このファイルには、プロジェクトの設計に関する重要な情報を記載します。

## 現在の技術スタック

### 基本情報
- **Java**: 21 (JDK 21以上必須)
- **Kotlin**: 1.9.25
- **Spring Boot**: 3.4.0
- **Gradle**: 8.14.3

### 主要なフレームワーク・ライブラリ

#### Spring関連
- Spring Boot Starter Web
- Spring Boot Starter Data JPA (HikariCPを除外)
- Spring Boot Starter Security
- Spring Boot Starter AOP
- Spring Boot Starter Cache
- Spring Boot Starter Validation
- Spring Boot DevTools (開発環境用)

#### API仕様
- **SpringDoc OpenAPI**: 2.7.0 (Swagger UI)
  - アクセスURL: http://localhost:8080/swagger-ui.html

#### キャッシュ
- **Caffeine**: 2.8.8 (インメモリキャッシュ)

### データベース関連
- **MySQL**: MySQL Connector/J 8.4.0 (環境変数で変更可能)
- **DBFlute**: 1.2.9 (タイプセーフなDB操作フレームワーク)
- **接続プール**: Apache Tomcat JDBC Pool 9.0.10
- データベース名: firewolfdb
- ユーザー名: firewolfuser

### 外部連携
- **Firebase Admin SDK**: 9.1.1 (JWT認証)
- **Twitter API**: Twittered 2.13
- **Slack API**: slack-api-client 1.4.0
- **Discord**: Webhook連携

### ビルドツール
- **Gradle**: 8.14.3
- **Jib**: 3.3.2 (Dockerイメージビルド)
  - ARM64向けLinuxイメージをビルド
  - ベースイメージ: eclipse-temurin:21

### テスト
- Spring Boot Starter Test
- Mockito Kotlin: 2.1.0

### 実行環境
- **JVM Target**: Java 21
- **コンテナ**: Dockerコンテナ (GitHub Container Registry使用)
- **デプロイ**: GitHub Actions + Kubernetes

### アプリケーション設定
- **ポート**: 8087
- **コンテキストパス**: /firewolf
- **CORS**: 設定可能（開発環境: http://localhost:3000）
- **圧縮**: 有効
- **アクセスログ**: Tomcat形式で出力

## アーキテクチャ概要

### レイヤー構成
- **api層** (`com.ort.firewolf.api`): REST APIコントローラー
- **application層** (`com.ort.firewolf.application`): ビジネスロジック、トランザクション境界
- **domain層** (`com.ort.firewolf.domain`): ドメインモデル、ビジネスルール
- **infrastructure層** (`com.ort.firewolf.infrastructure`): 外部システム連携、データアクセス実装

### 重要な技術的特徴
- **認証**: Firebase Admin SDKによるJWT検証
- **データアクセス**: DBFluteによるタイプセーフなDB操作
- **キャッシュ**: Caffeineによるインメモリキャッシュ
- **外部連携**: Twitter API、Slack API、Discord Webhook

## 設計記載のガイドライン

新しい設計を追加する際は、以下の項目を含めることを推奨します：

1. **概要**: 何を実装/変更するかの簡潔な説明
2. **背景**: なぜこの変更が必要なのか
3. **設計詳細**: 具体的な実装方法
4. **影響範囲**: 他のコンポーネントへの影響
5. **テスト計画**: どのようにテストするか