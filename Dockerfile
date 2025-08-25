FROM eclipse-temurin:21-jre

## 创建目录，并使用它作为工作目录
RUN mkdir -p /ai-code-mother
RUN mkdir -p /ai-code-mother/tmp/code_deploy
RUN mkdir -p /ai-code-mother/tmp/code_output
WORKDIR /ai-code-mother
## 将后端项目的 Jar 文件，复制到镜像中
COPY target/*.jar app.jar
## 设置 TZ 时区
## 设置 JAVA_OPTS 环境变量，可通过 docker run -e "JAVA_OPTS=" 进行覆盖
ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms1024m -Xmx1024m"

## 暴露后端项目的 48080 端口
EXPOSE 8123

## 启动后端项目
CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar app.jar