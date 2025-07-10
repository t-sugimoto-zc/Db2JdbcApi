# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/db2-jdbc-api-0.0.1-SNAPSHOT.jar app.jar
COPY lib/db2jcc4.jar lib/db2jcc4.jar

# JDBC接続情報の環境変数設定
COPY src/main/resources/db.properties config/db.properties

EXPOSE 8080
ENTRYPOINT ["java", "-cp", "app.jar:lib/db2jcc4.jar", "org.springframework.boot.loader.JarLauncher"]
