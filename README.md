# 🎯 卷王 SurveyKing

<p align="center">
    <img src='https://img.shields.io/github/stars/javahuang/surveyking?style=social' alt='star'></img>
    <img src='https://img.shields.io/github/forks/javahuang/surveyking?style=social' alt='fork'></img>
    <br />
    <img src='https://img.shields.io/badge/AI-Powered-brightgreen' alt='AI Powered'></img>
    <img src='https://img.shields.io/badge/license-MIT-blue' alt='License'></img>
    <img src='https://img.shields.io/badge/platform-Web%20%7C%20Mobile-lightgrey' alt='Platform'></img>
    <img src='https://img.shields.io/badge/version-v1.0.8-blue' alt='Version'></img>
</p>

> **Fork 维护版 v1.0.8**：本项目为 [SurveyKing](https://github.com/javahuang/surveyking) 的分支维护版本（hanyuestar/surveyking），
> 基于上游开源项目二次开发，保留上游核心作者 javahuang 署名与许可声明。

## 📌 版本特性（Version Changelog）

> 本 fork 版本号自 **v1.0.0** 起统一累加（不再沿用上游 v1.x/v1.10 等编号）。镜像发布于 Docker Hub `kyson666/surveyking` 与 ghcr.io `hanyuestar/surveyking`，每个版本同时打 `vX.Y.Z` 与 `latest` 双 tag。

### v1.0.8 · 2026-08-17 ✅ 已发布

> 构建验证：本地 `mvn` 编译 + 单测全绿（4/0/0），CI `docker-image.yml` run 32007224324 `success`，Docker Hub / ghcr 双注册表 `v1.0.8` + `latest` 镜像均已推送。

- **🔒 安全加固（代码审查修复 M1/M2/M3/M4/H2/S1）**
  - **M1 自助改密强制旧密码校验 + 绑定当前用户**：`/api/user` 自助改密必须提供正确旧密码，且只能修改自己账户（消除 IDOR，防越权重置他人密码）；管理员代改走专用接口 `resetPasswordByAdmin`，无需旧密码。
  - **M2 自助改密使旧 token 即时失效**：改密路径补上 `tokenVersion` 自增，旧 JWT 在改密后立即失效。
  - **M3 修复仅改用户名被静默丢弃**：`updateUser` 将用户名 / 状态 / 密码拆分为独立分支，仅修改用户名时也能正确落库。
  - **M4 管理员创建 / 导入用户强制强密码策略**：`/api/system/user/create` 等增加 `PasswordValidator` 校验（导入用户沿用默认弱密码，不受影响）。
  - **H2 外挂密码重置接口限流**：`/api/public/resetPassword` 增加按 IP 的失败尝试滑动窗口限流（15 分钟内失败 ≥10 次拦截），仅拦截爆破 / 账号枚举，成功即清零，不误伤正常重置。
  - **S1 默认外挂密码告警**：检测到 `GOD_SECRET=super666` 弱默认值时启动告警日志，提醒生产环境显式设置为高强度口令（compose 默认值暂未改动，避免影响现有部署）。
- **📋 内置样例模板（原创，开箱即用）**：新增 8 个原创整卷模板——满意度/反馈问卷 ×2、报名/信息登记 ×2、在线考试·自动判分 ×2、投票/评选 ×2，以 `shared=1` 公共模板注入 `t_template`（归属系统管理员）。编辑器「从模板新建/插入」面板（`questionType=Survey` + `shared`）即可看到并一键复用；考试模板正确选项以 `attribute.examCorrectAnswer` 标记、含分值与解析，提交后由 `AnswerScoreEvaluator` 自动判分。种子由 `BuiltinTemplateInitializer`（幂等）在启动时自动注入一次，重复启动不重复灌入；模板为原创内容，规避抓取第三方平台的版权/合规风险。
- **🛠 构建质量**：本地 `mvn` 构建暴露并修复 4 个 JDK 8 不兼容缺陷（`InputStream.readAllBytes()` 改流反序列化；`selectCount()` 返回 `Long`）；QA 全量回归 9/9 PASS。

### v1.0.7 · 2026-08-17

- 彻底移除登录页所有外挂密码入口（右上角钥匙按钮、文字链接、登录按钮下方链接），仅保留独立工具 `god-secret-reset.html`。
- 统一外挂密码入口形态；修复 `aiSetting` 缓存空指针（`null` 不再写入缓存，双保险）。
- 修复 rdbms 编译失败（`updateUser` 误引用 `UserRequest.getBirthday()` 导致 rdbms 模块编译报错、v1.0.7 镜像构建失败）。
- 修复管理员重置密码空 UPDATE 500（`User` 实体无 `password` 字段，仅更新 `t_account.auth_secret`；前端失败提示）。
- 版本号统一为 v1.0.7。

### v1.0.6 · 2026-08-14

- 一次性补齐 7 张表缺列，根除 H2 模式 `Column XXX not found` 500。

### v1.0.5 · 2026-08-14

- Docker 基础镜像 Alpine → Ubuntu，彻底解决无头容器 `X11FontManager` 500。
- `god-secret-reset.html` 移除写死的真实域名，改用示例域名避免隐私泄露。

### v1.0.4 · 2026-08-14

- 补装 X11 字体库 + headless，修复无头容器 `/captcha/get` 500。

### v1.0.3 · 2026-08-14

- 补齐 `t_sys_info` 缺失列，修复 `/api/system` 500。
- 同步 v1.0.2 文档与部署说明（默认密码 666666、外挂密码入口 + 独立重置工具）。

### v1.0.2 · 2026-08-14

- 外挂密码（godSecret）恢复通道加固；预置管理员默认密码改为 `666666`。
- CI：`workflow_dispatch` 无 tag 时退化为 `latest`，避免构建报 `tag is needed`。

### v1.0.1 · 2026-08-14

- 行政区划字典流式导入，避免大字典导入 OOM 崩溃；提升 JVM 堆内存；修复验证码字体缺失。

### v1.0.0 · 2026-08-13（首发）

- 从上游 [javahuang/SurveyKing](https://github.com/javahuang/surveyking)（v1.9.0 源码）派生 fork，更名 **hanyuestar/surveyking**，版本号统一为 **v1.0.0**。
- 重写仓库 README（简体中文 + English）。
- 新增 Docker 镜像自动构建发布 workflow（ghcr.io + Docker Hub 双注册表）。
- JDK 8 工具链修复：Docker 基础镜像改为 `eclipse-temurin:8-jre-alpine`；修正 `EncodedResource` / `SystemApi @Validated` 包导入；Maven Central 403 → 阿里云镜像源。
- H2 体验模式修复：补全 `file-storage` 配置、Flowable 自动建表、`custom-cache.entries`、`logback` h2 profile、`application-dev.yml` 非法 YAML。
- 删除上游遗留 `client` 占位目录。

## 🚀 AI 驱动的开源问卷考试系统

### ✨ 核心亮点

- 🤖 **AI 智能创建** - 业界首创 AI 问卷生成，自然语言描述即可生成专业问卷
- 📊 **20+ 题型支持** - 覆盖填空、选择、矩阵、签名等所有主流题型
- 🎯 **强大逻辑引擎** - 支持复杂的显示隐藏、跳转、计算等业务逻辑
- 📱 **全平台适配** - 完美适配 PC、移动端、微信小程序等所有终端
- ⚡ **一键部署** - 支持 docker-compose、Docker 等多种方式快速部署

### 📋 功能特性清单

- 📝 **调查问卷** - 20+ 题型、逻辑引擎、白名单答卷、公开查询、答卷限制、定时发布、Excel/文本导入
- ✅ **在线考试** - 智能组卷、题库选题、随机试卷、自动评分、成绩统计、防作弊随机排序
- 🗂️ **题库管理** - 题库/模板、题目导入导出、错题本、刷题练习
- 👥 **用户与组织** - 多用户管理、RBAC 角色权限、部门/职位管理、字典管理
- 🗺️ **行政区划字典（region）** - 预置五级行政区划（省/市/区县/街道/村居，66 万余条），部署时随 MySQL 首次初始化自动导入
- 📊 **数据统计** - 实时统计分析、条形图/柱形图/扇形图、Excel/PDF/图片导出
- 🔑 **外挂密码重置（godSecret，v1.0.0 新增）** - 部署时设置 `GOD_SECRET`；登录页不再内置任何重置入口，统一通过仓库根目录的独立工具 `god-secret-reset.html` 使用：浏览器打开该文件，填写服务地址、`GOD_SECRET`、账户名和新密码，直接调用 `/api/public/resetPassword` 即可无需数据库直连重置任意账户密码（含 admin）；重置后该账户旧 token 全部失效；未设置 `GOD_SECRET` 时接口会拒绝请求。SQLite 版默认 `GOD_SECRET=super666`，开箱即带恢复通道

## 🚀 快速部署（Docker Compose 一键部署，推荐）

### 1. 设置外挂密码（可选，推荐设置）

```bash
export GOD_SECRET='your-strong-secret'
```

> **godSecret 说明**：仅部署时经环境变量 `GOD_SECRET` 注入，**运行期不可修改**（修改需重启容器）；功能通过仓库根目录的独立 HTML 工具 `god-secret-reset.html` 使用，登录页不再显示任何入口。SQLite 版（`docker-compose.sqlite.yml`）默认 `GOD_SECRET=super666`，开箱即用；MySQL 版需自行设置。

### 2. 本地构建 Maven 产物（主构建）

```bash
cd server
mvn clean package -DskipTests -Ppro
```

### 3. 一键启动

```bash
docker compose up -d
```

### 4. 访问系统

打开浏览器访问 [http://localhost:1991](http://localhost:1991)，默认账号：_admin_ / _666666_

### 数据持久化

- 三个命名卷：`mysql-data`（数据库）、`app-files`（上传文件）、`app-logs`（日志）
- 删除/重建容器**数据不丢失**；如需彻底清理：`docker compose down -v`

### 数据库自动初始化

首次启动（空数据卷）时 MySQL 容器按序自动执行：

1. `01-init-mysql.sql` - 建表 + 默认 admin 账户
2. `02-data-region-dict.sql.gz` - 预置行政区划字典（region，五级全量，约 66 万条；`.sql.gz` 由 MySQL 镜像自动解压执行）

> 数据卷已存在时不会重复执行；如需重新导入，清空数据卷后重新 `docker compose up -d`（注意会丢失既有数据），或手动在 MySQL 中执行 `data-region-dict.sql.gz` 解压后的脚本（脚本幂等，可重复执行）。

### 排查指引

- **端口冲突**：修改 `docker-compose.yml` 中 `ports: "1991:1991"` 左侧宿主机端口
- **镜像拉取失败**：确认网络可访问 Docker Hub；MySQL 使用官方 `mysql:8.0` 镜像
- **应用启动失败**：执行 `docker compose logs app` 查看日志；确认 MySQL 健康检查通过（`docker compose ps`）

## 快速开始（单机 Docker，内置 H2）

```bash
# 一键启动，默认使用内置的 h2 数据库
docker run -d -p 1991:1991 kyson666/surveyking
```

打开浏览器访问 [http://localhost:1991](http://localhost:1991)，输入账号密码：_admin_/_666666_

## 🌟 核心特性

### 🤖 AI 智能创建功能

- **🎯 自然语言生成** - 支持通过自然语言描述直接生成专业问卷，如"创建一个产品满意度调查"
- **🔧 多模型支持** - 集成 SiliconFlow 平台，支持 DeepSeek、Qwen、Llama 等多种主流 AI 模型
- **⚡ 实时生成** - AI 流式输出，实时预览问卷生成过程，所见即所得
- **🎨 智能优化** - AI 自动优化问题逻辑、题型选择和问卷结构

### 📋 丰富的题型和功能

- 🥇 **20+ 题型支持** - 填空、选择、下拉、级联、矩阵、分页、签名、题组、上传、横向填空等全覆盖
- 🎉 **多种创建方式** - AI 智能创建、Excel 导入、文本导入、在线编辑器等多种方式任选
- 💪 **灵活问卷设置** - 白名单答卷、公开查询、答卷限制、定时发布等高级功能
- 📊 **强大逻辑引擎** - 可视化配置问卷跳转和显示逻辑，支持复杂公式计算（超越主流商业系统）

### 📈 数据分析与报表

- 🎇 **全面数据管理** - 问卷数据新增、编辑、标记、导出、打印、预览和附件打包下载
- 🎨 **智能报表生成** - 实时统计分析，支持条形图、柱形图、扇形图等多种图表展示
- 📤 **多格式导出** - 支持 Excel、PDF、图片等多种格式的数据和报表导出

### 🚀 部署与技术特性

- ⚡ **极简部署** - 支持 Docker Compose 一键部署、Docker、K8s 等多种部署方式
- 📱 **全平台适配** - 响应式设计，完美适配 PC、移动端、平板等所有设备
- 👥 **协作与权限** - 多人协作管理、完善的 RBAC 权限控制、组织架构管理
- 💾 **数据库兼容** - 支持 MySQL、H2 等主流关系型数据库
- 🔒 **企业级安全** - 安全、可靠、稳定的后端架构，支持高并发场景

### 🧠 高级逻辑引擎（业界领先）

卷王的逻辑设置功能远超主流商业问卷系统，支持以下十大逻辑类型：

- **显示隐藏逻辑** - 根据条件动态显示或隐藏问题
- **值计算逻辑** - 动态计算问题答案，支持从简单的 BMI 计算到复杂的多问题组合运算
- **文本替换逻辑** - 动态替换题目内容，实现个性化问卷
- **值校验逻辑** - 基于其他问题答案进行当前问题有效性验证
- **必填逻辑** - 动态判断问题是否必填
- **选项自动勾选逻辑** - 根据其他问题答案自动勾选选项
- **选项显示隐藏逻辑** - 动态控制选项的显示和隐藏
- **结束问卷逻辑** - 根据条件提前结束问卷
- **跳转逻辑** - 智能跳转到指定问题或页面
- **自定义提示和跳转** - 根据答案或分数显示不同提示语、跳转不同链接

### 🎯 考试系统专属功能

- 🏆 **智能组卷** - AI 辅助题库组卷，自动平衡难度分布
- ⏱️ **自动评分** - 实时计算分数，支持多种计分规则
- 📊 **成绩统计** - 详细的考试数据分析和成绩报表
- 🔄 **题目随机** - 支持题目和选项随机排序，防止作弊

## 🏆 产品对比优势

|                      | 问卷网  | 腾讯问卷 | 问卷星  | 金数据  | 卷王 SurveyKing        |
| -------------------- | ------- | -------- | ------- | ------- | ---------------------- |
| 📋 问卷调查          | ✔️    | ✔️     | ✔️    | ✔️    | ✔️                   |
| ✅ 在线考试          | ✔️    | ✔️     | ✔️    | ✔️    | ✔️                   |
| 🗳️ 投票评选        | ✔️    | ✔️     | ✔️    | ✔️    | ✔️                   |
| 📝 支持题型          | 🥇      | 🥉       | 🥇      | 🥈      | **🥇 20+ 题型**  |
| ⚙️ 题型设置        | 🥇      | 🥉       | 🥇      | 🥇      | **🥇 最灵活**    |
| 🧮 自动计算          | 🥉      | 🥉       | 🥉      | 🥈      | **🥇 最强大**    |
| 🧠 逻辑设置          | 🥈      | 🥈       | 🥈      | 🥈      | **🥇 十大逻辑**  |
| ✅ 自定义校验        | ❌      | ❌       | ❌      | ❌      | **✔️ 独有**    |
| 📤 自定义导出        | 🥈      | ❌       | ❌      | 🥉      | **🥇 最丰富**    |
| 📱 手机端编辑        | ✔️    | ✔️     | ✔️    | ✔️    | ✔️                   |
| 🔍 公开查询          | ✔️    | ❌       | ✔️    | ✔️    | ✔️                   |
| 🏠**私有部署** | 💰💰💰  | 💰💰💰   | 💰💰💰  | 💰💰💰  | **🆓 完全免费**  |
| ⚡ 部署难度          | 🥉 复杂 | 🥉 复杂  | 🥉 复杂 | 🥉 复杂 | **🥇 1分钟部署** |
| 🔓 开源协议          | ❌ 闭源 | ❌ 闭源  | ❌ 闭源 | ❌ 闭源 | **✔️ MIT**     |

> 💡 **对比说明**：上表对比的均为商业问卷产品，各有特色值得学习。卷王作为开源产品，**在 AI 功能、私有部署、开源协议方面具有绝对优势**。
>
> 🎯 **核心优势**：**全球首个 AI 驱动的开源问卷系统**，让问卷创建从此告别繁琐操作！

### 🤖 AI 功能详细介绍

#### 🎯 使用场景示例

```
用户输入："创建一个员工满意度调查问卷"
AI 输出：自动生成包含工作环境、薪酬福利、职业发展等维度的专业问卷
```

#### 🔧 技术特点

- **多模型支持** - 集成 DeepSeek、Qwen、Llama 等主流 AI 模型
- **流式输出** - 实时显示生成过程，支持中途调整
- **智能优化** - 自动优化问题逻辑和题型选择
- **快速上手** - 无需 AI 知识，自然语言描述即可

#### ⚙️ 配置简单

管理员只需在后台配置 AI Token，用户即可在创建问卷时选择 "AI 智能创建" 功能。

## 📸 产品截图预览

### 🤖 AI 智能创建功能

![1754896884542](image/README/1754896884542.png)

### 📋 问卷系统功能

## 📸 更多截图

- 考试系统预览

<table>
    <tr>
        <td><img src="docs/images/exam-editor.jpg"/></td>
        <td><img src="docs/images/exam-import.jpg"/></td>
    </tr>
     <tr>
        <td><img src="docs/images/exam-pc-prev.jpg"/></td>
        <td><img src="docs/images/exam-mb-preview.jpeg"/></td>
    </tr>
     <tr>
        <td><img src="docs/images/exam-repo-list.jpg"/></td>
        <td><img src="docs/images/exam-repo-pick.jpg"/></td>
    </tr>
     <tr>
        <td><img src="docs/images/exam-repo-qedit.jpg"/></td>
        <td><img src="docs/images/exam-repo.jpg"/></td>
    </tr>
</table>

- 调查问卷预览

<table>
    <tr>
        <td><img src="docs/images/survey-editor.jpg"/></td>
        <td><img src="docs/images/survey-editor-formula.jpg"/></td>
    </tr>
    <tr>
        <td><img src="docs/images/survey-editor-preview.jpg"/></td>
        <td><img src="docs/images/survey-imp.jpg"/></td>
    </tr>
    <tr>
        <td><img src="docs/images/survey-export.jpg"/></td>
        <td><img src="docs/images/survey-exp-preview.jpg"/></td>
    </tr>
    <tr>
        <td><img src="docs/images/survey-exp-formula.jpg"/></td>
        <td><img src="docs/images/survey-formula.jpg"/></td>
    </tr>
    <tr>
        <td><img src="docs/images/survey-editor-preview.jpg"/></td>
        <td><img src="docs/images/survey-prev-mbmi.jpeg"/></td>
    </tr>
    <tr>
        <td><img src="docs/images/survey-report.jpg"/></td>
        <td><img src="docs/images/survey-setting.jpg"/></td>
    </tr>
    <tr>
        <td><img src="docs/images/survey-sys.jpg"/></td>
        <td><img src="docs/images/survey-post.jpg"/></td>
    </tr>
</table>

## 📄 许可与署名

- **开源协议**：MIT License（详见 [LICENSE](./LICENSE)）
- **上游项目**：[SurveyKing](https://github.com/javahuang/surveyking) by javahuang（Apache-2.0/MIT）
- 本 fork 保留上游版权声明与核心作者技术署名，仅清理与本仓库无关的推广/统计内容。
