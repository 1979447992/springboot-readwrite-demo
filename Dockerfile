# 使用与Spring Boot 3.1.5兼容的Maven版本
FROM maven:3.8-openjdk-17-slim AS build

# 设置工作目录
WORKDIR /app

# 复制pom.xml文件
COPY pom.xml .

# 下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 编译项目
RUN mvn clean package -DskipTests

# 运行时镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=build /app/target/springboot-readwrite-demo-1.0.0.jar app.jar

# 创建应用用户
RUN adduser --disabled-password --gecos '' appuser

# 更改文件所有者
RUN chown -R appuser:appuser /app

# 切换到应用用户  
USER appuser

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
