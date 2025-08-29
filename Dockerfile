# 使用OpenJDK 17作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 创建应用用户
RUN adduser --disabled-password --gecos '' appuser

# 复制Maven构建的jar文件
COPY target/springboot-readwrite-demo-1.0.0.jar app.jar

# 更改文件所有者
RUN chown appuser:appuser app.jar

# 切换到应用用户
USER appuser

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]