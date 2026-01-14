# --- GIAI ĐOẠN 1: BUILD ---
# Dùng ảnh chứa Maven và JDK 17 để build code
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Tạo thư mục làm việc trong container
WORKDIR /app

# Copy file pom.xml
COPY pom.xml .

# Tải trước các dependency (go-offline) giúp build nhanh hơn
RUN mvn dependency:go-offline

# Copy toàn bộ source code
COPY src ./src

# Build ra file .jar (Skip test để build cho nhanh)
RUN mvn clean package -DskipTests

# --- GIAI ĐOẠN 2: RUN ---
# Dùng ảnh chỉ chứa JRE (Java Runtime)
FROM eclipse-temurin:17-jre-alpine

# Tạo thư mục làm việc
WORKDIR /app

# Copy file .jar TỪ giai đoạn build sang giai đoạn run
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8080 (Cổng mặc định của Spring Boot)
EXPOSE 8080

# Lệnh chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "app.jar"]