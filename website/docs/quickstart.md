---
id: quickstart
title: 快速开始
sidebar_label: 快速开始
---

## 安装

### 通过 Docker Compose 一键部署（推荐）

1. 设置外挂密码（可选，推荐设置）：

```bash
export GOD_SECRET='your-strong-secret'
```

> **godSecret 说明**：仅部署时经环境变量 `GOD_SECRET` 注入，**运行期不可修改**（修改需重启容器）；未设置时登录页不显示钥匙按钮，功能关闭。

2. 本地构建 Maven 产物（主构建）：

```bash
cd server
mvn clean package -DskipTests -Ppro
```

3. 一键启动：

```bash
docker compose up -d
```

4. 打开浏览器访问 [http://localhost:1991](http://localhost:1991)，默认账号：_admin_ / _123456_

数据持久化：三个命名卷（`mysql-data`、`app-files`、`app-logs`），删除/重建容器数据不丢失。

### 使用源码编译安装

默认构建的是 h2 版本的安装包。

使用 gradle 构建：

```bash
# 设置 profile，修改 api/src/main/resources/application.yml
# 打开 active: ${activeProfile} # gradle 配置

# 开始构建
gradle clean :api:build -P pro -x test
```

使用 maven 构建：

```bash
# 开始构建
mvn clean package -DskipTests -Ppro
# 生成的 jar 包位于 ./api/target/surveyking-v1.0.0.jar
```

### 使用 docker 快速启动（内置 H2）

```bash
docker run -p 1991:1991 surveyking/surveyking
# 挂载数据文件
docker run -d -p 1991:1991 -v /my/files:/files -v /my/logs:/logs
```

## 使用

- **预安装 JRE 环境**，由于本系统是 Java 构建的，需要依赖 Java 运行环境，可以通过 [适用于所有操作系统的 Java 下载
  ](https://www.java.com/zh-CN/download/manual.jsp) 来预装 java 环境。
- **配置数据库**，按照下面的说明来配置不同的数据库，如果前端需要使用 nginx 部署，参考使用 nginx 部署前端。
- **运行**，支持所有平台部署，windows 和 mac 支持双击运行，或者打开命令行窗口执行如下命令

```bash
java -jar surveyking-v1.0.0.jar
```

打开浏览器，访问 [http://localhost:1991](http://localhost:1991) 即可，系统首次启动之后会自动创建 admin 用户，账号/密码（*admin/123456*），登录系统之后可以通过用户管理界面来修改密码。

<!-- ### h2 启动方式

无需任何配置，会自动创建数据库启动脚本，如需改变端口号，参考 mysql 启动方式的定义端口。 -->

### mysql 启动方式

使用参数启动

1. 首先创建 mysql 数据库，然后执行初始化脚本（见 `server/rdbms/src/main/resources/scripts/init-mysql.sql`）。
2. 执行 `java -jar surveyking-v1.0.0.jar --server.port=1991 --spring.datasource.url=jdbc:mysql://localhost:3306/surveyking --spring.datasource.username=root --spring.datasource.password=123456`（只有首次启动系统需要添加后面的参数）

参数说明(按照实际需要自行修改)：

- `--server.port=1991` 系统端口
- `--spring.datasource.url=jdbc:mysql://localhost:3306/surveyking` 数据库连接的 url
- `--spring.datasource.username=root` 数据库账号
- `--spring.datasource.password=123456` 数据库密码

也可以尝试使用命令行的方式初始化数据库（会自动执行数据库初始脚本）

```bash
# 按照提示初始化数据库
java -jar surveyking-v1.0.0.jar i
# 初始化完成之后运行即可
java -jar surveyking-v1.0.0.jar 
```

### 使用 nginx 部署前端

直接部署仓库内 `server/api/src/main/resources/static` 目录下的静态资源到 nginx 即可。

然后配置 proxy 代理到后端 api 服务。
