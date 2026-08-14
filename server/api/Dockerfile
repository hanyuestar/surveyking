# openjdk:8-jdk-alpine 已被 Docker Hub 官方归档移除（2024 年），改用 Temurin 8 JRE Alpine
# 参考：https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:8-jre-alpine
ARG JAR_FILE=api/target/surveyking-v1.0.0.jar
COPY ${JAR_FILE} app.jar
# 说明：原 Dockerfile 会从构建上下文复制内嵌 H2 数据库文件 surveyking.mv.db，
# 该文件在本仓库 server/ 目录下不存在（实际位于 scripts/database/survey.mv.db），
# 且 docker-compose 部署使用 MySQL（见仓库根目录 docker-compose.yml），
# 无需内嵌 H2 数据库，故移除该 COPY，避免构建失败。
# COPY surveyking.mv.db surveyking.mv.db
RUN mkdir /surveyking
# 安装 fontconfig（内含 freetype 原生库）：AJ-Captcha 在无头容器里加载水印字体
# 报 "Problem reading font data"，根因是缺少字体栅格化依赖，装 fontconfig 即可修复；
# ttf-dejavu 提供基础西文字形（水印文本为英文 survey），避免登录验证码水印渲染异常。
RUN apk add --no-cache fontconfig ttf-dejavu
VOLUME /files
VOLUME /logs
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]