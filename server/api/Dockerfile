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
# 修复无头容器验证码 500（sun.awt.X11FontManager 静态初始化失败 / NoClassDefFoundError）：
# Temurin 8 在 Alpine 上的 libfontmanager.so 除依赖 fontconfig+freetype 外，还硬依赖 X11 客户端库
# （libX11 等）。仅装 fontconfig 不够，X11FontManager 类在首次加载字体时静态初始化失败，
# 之后整 JVM 生命周期内该类被标记为"无法初始化"，导致 /captcha/get 永久 500。
# 故需补装 X11 客户端库 + 字体，并强制 -Djava.awt.headless=true 避免需要 DISPLAY。
RUN apk add --no-cache fontconfig ttf-dejavu ttf-liberation \
    libx11 libxext libxrender libxtst libxi \
    libxrandr libxcomposite libxdamage libxcursor \
    && fc-cache -f
VOLUME /files
VOLUME /logs
ENTRYPOINT ["sh", "-c", "java -Djava.awt.headless=true ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]