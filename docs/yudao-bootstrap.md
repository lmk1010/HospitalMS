# Yudao 单体版初始化说明

## 当前已完成
- `backend/`：已接入官方 Yudao 单体后端骨架
- `backend/pom.xml`：已开启 `yudao-module-bpm`
- `backend/yudao-server/pom.xml`：已引入 `yudao-module-bpm`
- `frontend/`：已接入官方 `yudao-ui-admin-vue3`
- `sql/yudao/hospital_ms-init.sql`：一键初始化 SQL（推荐）
- `sql/yudao/bpm-2024-03-30.sql`：完整 BPM 业务表示例 SQL（含较多示例流程数据）
- `.run/Backend_Local.run.xml`：IntelliJ IDEA 后端运行配置
- `.run/Frontend_Dev.run.xml`：IntelliJ IDEA 前端运行配置

## 免费 / 付费结论
- Yudao 官方明确说明：现在、未来都不会有商业版本，代码全部开源
- BPM 工作流是开源能力，不是单独收费模块
- 你自己要准备的是 MySQL、Redis、JDK、Node 这些运行环境

## 本地默认配置
### 后端 Local
- 配置文件：`backend/yudao-server/src/main/resources/application-local.yaml`
- 端口：`48080`
- 数据库：`hospital_ms`
- 用户名：`root`
- 密码：`123456`

### 后端 Prod
- 配置文件：`backend/yudao-server/src/main/resources/application-prod.yaml`
- 使用环境变量：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`
- Redis 变量：`REDIS_HOST`、`REDIS_PORT`、`REDIS_DATABASE`、`REDIS_PASSWORD`
- RabbitMQ 变量：`RABBITMQ_HOST`、`RABBITMQ_PORT`、`RABBITMQ_USERNAME`、`RABBITMQ_PASSWORD`
- 生产环境默认关闭 Swagger / Knife4j，关闭 Flowable 自动建表

### 前端 Local
- 地址：`http://localhost:48080/admin-api`
- 配置文件：`frontend/.env.local`

## 建库步骤
```sql
CREATE DATABASE hospital_ms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 导入方式
### 推荐：一键导入
- 直接导入：`sql/yudao/hospital_ms-init.sql`

### 分步导入
- `sql/yudao/ruoyi-vue-pro.sql`
- `sql/yudao/quartz.sql`
- `sql/yudao/bpm-2024-03-30.sql`

## 说明
- `hospital_ms-init.sql` 已合并基础系统表、Quartz 表、BPM 业务表结构
- `hospital_ms-init.sql` 只包含 `bpm_*` 业务表结构，不包含 `Flowable act_*` 表
- `Flowable act_*` 表由应用在 `local` 环境启动时自动创建
- 如果你想导入完整 BPM 示例流程数据，再额外执行 `sql/yudao/bpm-2024-03-30.sql`

## 建议运行环境
- JDK 17
- Maven 3.9+
- MySQL 8
- Redis 7
- Node 20+
- pnpm 9+

## 启动命令
### 后端 Local
```bash
cd backend
mvn -pl yudao-server -am org.springframework.boot:spring-boot-maven-plugin:2.7.18:run -DskipTests -Dspring-boot.run.profiles=local
```

### 前端 Local
```bash
cd frontend
pnpm install
pnpm dev:antd
```

## IntelliJ IDEA
- 打开项目后，IDEA 会识别 `.run/` 里的共享配置
- 直接运行 `Backend Local`
- 直接运行 `Frontend Dev`（Vben5 Ant Design，默认 http://localhost:5666）

## 下一步改造建议
- 先保留 Yudao 的系统管理、基础设施、BPM
- 新增业务模块：患者、预约、勾画、计划、治疗、费用
- 审批类流程全部挂到 BPM
- 登记、排队、列表、统计先走普通 CRUD
