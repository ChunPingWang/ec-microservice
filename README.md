# 電商多微服務系統

基於 Spring Boot 3 和 Java 17 的微服務架構電商平台，採用領域驅動設計（DDD）和六角形架構（Hexagonal Architecture），嚴格遵循 SOLID 設計原則。

## 專案結構

```
ecommerce-microservices/
├── common/                 # 共用模組
├── api-gateway/           # API 閘道 (Port: 8080)
├── customer-service/      # 客戶微服務 (Port: 8081)
├── product-service/       # 商品微服務 (Port: 8082)
├── order-service/         # 訂單微服務 (Port: 8083)
├── payment-service/       # 付款微服務 (Port: 8084)
├── logistics-service/     # 物流微服務 (Port: 8085)
└── sales-service/         # 銷售微服務 (Port: 8086)
```

## 技術棧

- **語言**: Java 17
- **框架**: Spring Boot 3.2.0
- **建構工具**: Gradle 8.x
- **資料庫**: 
  - 本機開發: H2 (記憶體資料庫)
  - SIT/UAT/生產: PostgreSQL 15
- **快取**: Redis
- **訊息佇列**: RabbitMQ
- **測試**: JUnit 5, Mockito, Cucumber, TestContainers, REST Assured

## 架構原則

### SOLID 設計原則

1. **單一職責原則 (SRP)**: 每個類別只有一個變更的理由
2. **開放封閉原則 (OCP)**: 對擴展開放，對修改封閉
3. **里氏替換原則 (LSP)**: 子類別可以替換父類別
4. **介面隔離原則 (ISP)**: 客戶端不應依賴不需要的介面
5. **依賴反轉原則 (DIP)**: 依賴抽象而非具體實作

### 六角形架構

每個微服務都採用三層架構：

- **Domain Layer**: 領域邏輯和業務規則
- **Application Layer**: 使用案例和應用服務
- **Infrastructure Layer**: 技術實作和外部整合

## 快速開始

### 前置需求

- Java 17
- Docker & Docker Compose
- MySQL 8.0
- Redis
- RabbitMQ

### 建構專案

```bash
# 建構所有模組
./gradlew build

# 建構特定服務
./gradlew :customer-service:build
```

### 執行服務

```bash
# 啟動 API Gateway
./gradlew :api-gateway:bootRun

# 啟動客戶服務
./gradlew :customer-service:bootRun
```

## 開發指南

### 新增微服務

1. 在 `settings.gradle` 中新增模組
2. 建立服務目錄和 `build.gradle`
3. 實作六角形架構的三層結構
4. 遵循 SOLID 原則進行設計

### 測試先行開發 (TDD/BDD)

本專案採用測試先行開發方法論：

1. **BDD 功能測試**: 使用 Cucumber 和 Gherkin 語法定義業務需求
2. **單元測試**: 使用 JUnit 5 和 Mockito 測試領域邏輯
3. **整合測試**: 使用 TestContainers 提供真實的 PostgreSQL 環境
4. **API 測試**: 使用 REST Assured 測試 RESTful API

#### 測試執行順序

```bash
# 1. 執行 BDD 功能測試
./gradlew :customer-service:test --tests "*CucumberTestRunner"

# 2. 執行單元測試
./gradlew :customer-service:test --tests "*UnitTest"

# 3. 執行整合測試
./gradlew :customer-service:test --tests "*IntegrationTest"

# 4. 執行所有測試
./gradlew test
```

## API 文件

各微服務啟動後可透過以下端點查看 API 文件：

- API Gateway: http://localhost:8080/swagger-ui.html
- Customer Service: http://localhost:8081/swagger-ui.html
- Product Service: http://localhost:8082/swagger-ui.html

## 監控

所有服務都提供 Actuator 端點進行監控：

- 健康檢查: `/actuator/health`
- 指標: `/actuator/metrics`
- 資訊: `/actuator/info`

## 環境配置

### 本機開發環境

本機開發使用 H2 記憶體資料庫，無需額外安裝資料庫：

```bash
# 啟動服務後可透過以下網址存取 H2 控制台
http://localhost:8081/h2-console

# 連線設定
JDBC URL: jdbc:h2:mem:customer_db
User Name: sa
Password: (空白)
```

### 不同環境配置

```bash
# 本機開發環境 (預設)
./gradlew :customer-service:bootRun

# SIT 環境
./gradlew :customer-service:bootRun --args='--spring.profiles.active=sit'

# UAT 環境
./gradlew :customer-service:bootRun --args='--spring.profiles.active=uat'

# 生產環境
./gradlew :customer-service:bootRun --args='--spring.profiles.active=prod'
```

### 測試資料庫

- **單元測試**: 使用 H2 記憶體資料庫
- **整合測試**: 使用 TestContainers 啟動 PostgreSQL 容器
- **BDD 測試**: 使用 H2 記憶體資料庫進行快速測試

## BDD 測試報告

執行 Cucumber 測試後，可在以下位置查看測試報告：

- HTML 報告: `target/cucumber-reports/index.html`
- JSON 報告: `target/cucumber-reports/Cucumber.json`
- JUnit XML: `target/cucumber-reports/Cucumber.xml`