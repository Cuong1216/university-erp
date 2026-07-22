# ==========================================
# Stage 1: Build Spring Boot application
# ==========================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy pom.xml và tải dependencies trước để tận dụng Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và tiến hành đóng gói (jar)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Lightweight Runtime
# ==========================================
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Tạo non-root user/group để tăng cường bảo mật container
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy file jar đã build từ builder stage sang runtime stage
COPY --from=builder /app/target/*.jar app.jar

# Thiết lập Timezone (tùy chỉnh theo yêu cầu hệ thống)
ENV TZ=Asia/Ho_Chi_Minh

# Khai báo port ứng dụng Spring Boot
EXPOSE 8080

# Chạy ứng dụng với cờ tối ưu hóa bộ nhớ JVM trong môi trường container
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
