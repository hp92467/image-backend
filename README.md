# 设备故障图像协同诊断智能平台（后端）

---

## 适用场景：故障诊断案例图片管理

- **集中存储**：将各设备/工艺/线路的故障图片按空间与分类集中存储，保留清晰度、主色调、尺寸、格式等元数据。
- **多维检索**：按关键词（名称、简介、分类、标签）、主色调（近似颜色）、时间范围等快速检索定位典型故障样例。
- **分级空间**：
  - 公共图库：沉淀已审核的标准故障样例，供全员检索学习；
  - 私有空间：个人整理与标注阶段，不影响他人；
  - 团队空间：团队共同维护某产品线/区域/客户的故障库，支持权限控制与协同编辑。
- **质量管控**：内置审核流（管理员审核通过后进入公共图库），保证知识库输出的权威性与一致性。
- **批量治理**：支持批量上传、批量重命名、统一补充分类和标签，加速历史资料的治理与整编。
- **使用分析**：按空间维度分析体量、标签分布、尺寸分布与时间趋势，为容量规划与治理提供数据支撑。

---

## 功能清单

- **图片管理**：上传（文件/URL/批量）、编辑、删除、缩略图生成、WebP 压缩、主色提取。
- **审核流**：管理员审核通过/拒绝，支持审核备注与审核人、审核时间留痕。
- **空间模型**：公共图库、私有空间、团队空间（支持空间级别与配额：`maxSize`、`maxCount`）。
- **检索与分页**：名称/简介模糊检索、分类、标签、尺寸、格式、时间段过滤，分页返回 `PictureVO`。
- **颜色相似检索**：按目标颜色（例如 `#FF0000`）从指定空间内按相似度排序返回前 N 张。
- **批量能力**：批量上传、批量编辑（重命名规则、统一分类/标签）。
- **分析与报表**：空间用量、分类分布、标签 Top、尺寸区间、时间趋势、空间排行（管理员）。
- **缓存与加速**：Caffeine 本地缓存 + Redis 二级缓存的分页结果缓存，带随机过期防雪崩。
- **对象存储**：接入腾讯云 COS，上传时同步生成缩略图与 WebP 压缩图，返回原图信息与处理结果。
- **会话与鉴权**：Spring Session 基于 Redis；`@AuthCheck` 注解实现管理员等角色鉴权；细粒度资源权限。

---

## 技术栈

- 运行框架：Spring Boot 2.7.6、Spring Web、Spring AOP、Spring WebSocket
- 数据访问：MyBatis-Plus、MyBatis、MySQL
- 会话与缓存：Redis、Spring Session、Caffeine
- 对象存储：腾讯云 COS（`cos_api 5.6.x`）
- 工具库：Hutool、Jsoup（批量抓取图片）、Knife4j（OpenAPI 文档）

详见 `pom.xml` 与 `src/main/resources/application.yml`。

---

## 目录结构（后端）

- `src/main/java/com/hp/imagebackend`
  - `controller`：`PictureController`、`UserController`、`SpaceController`、`SpaceAnalyzeController` 等 REST 接口
  - `service`、`service/impl`：业务逻辑实现（图片上传/审核/检索/分析/批量等）
  - `manager`：`CosManager`、`upload`（上传模板、URL/文件两种实现）
  - `model`：`entity`/`dto`/`vo`/`enums`（图片、空间、用户与请求返回模型）
  - `config`：`CosClientConfig`、`MyBatisPlusConfig`、`CorsConfig` 等
  - `utils`：工具类（如颜色相似度计算）
- `src/main/resources`
  - `application.yml`：端口、上下文路径、数据源、Redis、COS 配置
  - `sql/`：建表/示例 SQL（如有）

---

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.x（创建数据库 `image-backend`）
- Redis 5.x+
- 腾讯云 COS（准备 `secretId/secretKey/region/bucket`）

### 配置
编辑 `src/main/resources/application.yml`：
- 服务与上下文：
  - `server.port: 8123`，`server.servlet.context-path: /api`
- 数据源：`spring.datasource.url/username/password`
- Redis：`spring.redis.host/port`
- 会话：`spring.session.store-type: redis`
- COS：`cos.client.host/secretId/secretKey/region/bucket`

### 初始化数据库
- 在 MySQL 中创建数据库：`image-backend`
- 执行 `sql/` 下的表结构脚本（如存在）；否则依据实体与 Mapper 初始化表结构

### 启动
```bash
mvn clean package -DskipTests
java -jar target/image-backend-0.0.1-SNAPSHOT.jar
```
启动后访问接口文档（Knife4j）：`http://localhost:8123/api/doc.html`

---

## 核心模型与权限

### 图片实体（节选）
- `url`、`thumbnailUrl`、`name`、`introduction`、`category`、`tags`
- `picSize`、`picWidth`、`picHeight`、`picScale`、`picFormat`、`picColor`
- `userId`、`spaceId`（`null` 表示公共图库）、审核字段（`reviewStatus/reviewerId/reviewTime/reviewMessage`）

### 空间模型
- 公共图库：`spaceId = null`，仅管理员可审核管理公共库内容
- 私有空间：每用户唯一私有空间，只有创建者可操作
- 团队空间：支持空间级别与配额（`maxSize/maxCount`），由空间创建者（或管理员）管理

### 权限规则（关键片段）
- `@AuthCheck(mustRole = ADMIN)`：管理员专属接口（如审核、后台分页查询、空间更新、排行）
- 图片操作：
  - 公共图库：创建者或管理员
  - 私有/团队空间：空间创建者（或具备授权的成员，若前端/扩展实现）

---

## 关键能力与实现要点

### 上传链路（URL/文件/批量）
- 控制器：`/upload`（文件）、`/upload/url`（URL）、`/upload/batch`（批量）
- 模板化上传：`PictureUploadTemplate` 统一校验、临时文件处理、COS 上传、抽取图片信息（尺寸/格式/主色/大小）
- COS 处理：
  - 同步生成 WebP 压缩版本（加速分发）
  - 生成缩略图（默认 128x128，原图 >2KB 时启用）
- 结果落库：保存图片元数据与空间、审核状态等；非管理员默认进入“待审核”

### 分页与缓存
- 分页查询 `list/page/vo/cache`：
  - 基于查询条件 JSON + MD5 生成 key
  - 本地 Caffeine 5 分钟缓存 + Redis 随机过期（300 ~ 600 秒）防雪崩

### 审核流
- 管理员：直接通过并记录审核人、时间与备注
- 普通用户：编辑/上传变更进入“待审核”

### 颜色相似检索
- 接口：`/search/color`，输入 `spaceId` 与目标 `picColor`（如 `#FF0000`）
- 在空间内提取主色已设置的图片，按相似度排序取 Top 12

### 分析与报表
- `POST /space/analyze/usage`：空间容量（已用大小/数量 + 使用率）
- `POST /space/analyze/category`：分类分布（count/totalSize）
- `POST /space/analyze/tag`：标签热度 Top
- `POST /space/analyze/size`：大小区间分布
- `POST /space/analyze/user`：按天/周/月的时间序列上传量
- `POST /space/analyze/rank`：空间容量排行（管理员）

---

## 面向故障诊断案例的最佳实践

- **空间规划**：
  - 每条产品线/区域一个团队空间；个人预处理用私有空间；公共库承载审核后的标准案例。
- **分类与标签**：
  - 分类建议以“设备类型/部位/工序”为主轴；
  - 标签记录“故障现象/原因/处理方法/严重度/环境条件”等，便于组合检索。
- **颜色检索**：
  - 对典型表面缺陷（锈蚀、油污、烧蚀）等可用主色近似匹配辅助定位。
- **批量治理**：
  - 用批量重命名规则 `{序号}` 快速生成一致命名；
  - 批量补齐分类与标签，保证数据一致性。
- **质量控制**：
  - 公共库内容必须经管理员审核；
  - 建议建立命名、标签、审核 checklist。
- **容量规划**：
  - 使用空间分析接口观察增长趋势与区间分布，及时扩容或归档。

---

## 常用接口速览（后端）

- 认证与用户
  - `POST /user/register`、`POST /user/login`、`POST /user/logout`
  - `GET /user/get/login` 获取当前登录用户
- 图片
  - `POST /upload`（管理员）文件上传
  - `POST /upload/url` URL 上传
  - `POST /upload/batch`（管理员）批量抓取上传
  - `POST /edit` 编辑图片（创建者/管理员）
  - `POST /delete` 删除图片（鉴权同上）
  - `POST /list/page/vo/cache` 带缓存的分页查询
  - `POST /review`（管理员）审核
  - `POST /search/color` 颜色相似检索
- 空间
  - `POST /space/update`（管理员）更新空间与级别（自动填充配额）
  - `GET /space/list/level` 获取空间级别与默认配额
- 分析
  - 见上一章节的 `/space/analyze/*` 系列

> 实际完整入参/出参以 Knife4j 文档为准：`/api/doc.html`

---

## 配置要点与安全建议

- 将 COS `secretId/secretKey`、数据库密码等放入环境变量或外部配置中心，避免明文提交。
- 针对公共接口开启 CORS 白名单；`CorsConfig` 可按域名细化策略。
- 生产环境建议：
  - Redis、MySQL、COS 网络策略与访问凭据应最小化授权
  - 开启 Nginx/网关进行静态加速与限流
  - 结合 WAF 与审计日志，监控关键接口

---

## 构建与部署

- 构建：`mvn clean package -DskipTests`
- 运行：`java -jar target/image-backend-0.0.1-SNAPSHOT.jar`
- 运行参数可通过 `--spring.profiles.active=prod` 切换不同环境（建议新增 `application-prod.yml` 按需覆盖）

---

## 常见问题（FAQ）

- 启动后 8123 端口无法访问？
  - 检查 `server.port` 与系统防火墙；`context-path` 为 `/api`。
- 图片上传失败？
  - 检查 COS 配置与密钥权限；确认桶区域、域名 `host` 与存储路径；查看日志。
- 分页接口速度慢？
  - 确认 Redis 正常、Caffeine 命中率；检查查询条件是否命中索引（建议对高频字段建索引）。
- 颜色检索结果不准？
  - 依赖主色提取（COS 返回 `ave`），若历史数据缺失主色，可通过离线任务补齐。

##  联系方式

- 项目维护者: HP
- 邮箱: hp92467@163.com
- 项目链接: (https://github.com/hp92467/image-backend)

---

## 许可

用于学习与内部项目二次开发。若需商用，请根据企业合规要求与第三方服务（COS 等）的协议进行。 
