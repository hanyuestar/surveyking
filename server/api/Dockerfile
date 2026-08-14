# openjdk:8-jdk-alpine 已被 Docker Hub 官方归档移除（2024 年），改用 Temurin 8 JRE。
# 由于 Alpine(musl) 上 Temurin 8 的 libfontmanager.so + X11 客户端库组合不稳定，
# 实际部署仍出现 sun.awt.X11FontManager 静态初始化失败，导致 /captcha/get 500。
# 故改用基于 glibc 的 Ubuntu 变体，字体/X11 依赖更完整，验证码在无头容器稳定运行。
# 参考：https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:8-jre

ARG JAR_FILE=api/target/surveyking-v1.0.0.jar
COPY ${JAR_FILE} app.jar

# 说明：原 Dockerfile 会从构建上下文复制内嵌 H2 数据库文件 surveyking.mv.db，
# 该文件在本仓库 server/ 目录下不存在（实际位于 scripts/database/survey.mv.db），
# 且 docker-compose 部署使用 MySQL（见仓库根目录 docker-compose.yml），
# 无需内嵌 H2 数据库，故移除该 COPY，避免构建失败。
# COPY surveyking.mv.db surveyking.mv.db

RUN mkdir /surveyking

# 修复无头容器验证码 500（sun.awt.X11FontManager 静态初始化失败 / NoClassDefFoundError）：
# JDK 8 在 Linux 下唯一字体管理器 X11FontManager 的 libfontmanager.so 依赖 fontconfig、
# freetype、X11 客户端库及中/西文字体。Ubuntu 变体通过 apt 可一次性装齐，无需手动处理 musl 兼容。
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig \
    fonts-dejavu-core \
    fonts-liberation \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libxrandr2 \
    libxcomposite1 \
    libxdamage1 \
    libxcursor1 \
    && fc-cache -f \
    && rm -rf /var/lib/apt/lists/*

VOLUME /files
VOLUME /logs
ENTRYPOINT ["sh", "-c", "java -Djava.awt.headless=true ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]
