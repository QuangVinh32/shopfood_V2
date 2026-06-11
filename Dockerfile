# syntax=docker/dockerfile:1.6

# ---- Stage 1: Build JAR bang Maven + JDK 17 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache dependencies truoc de tang toc build lan sau
COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

# Copy source roi build
COPY src ./src
RUN mvn -B -q -e -DskipTests package \
 && cp target/*.jar app.jar

# ---- Stage 2: Runtime chi can JRE 17 ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Tao user khong phai root cho an toan
RUN useradd -r -u 1001 -g root spring
COPY --from=build /workspace/app.jar /app/app.jar
RUN mkdir -p /app/uploads/images && chown -R 1001:0 /app
USER 1001

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"
ENV APP_UPLOAD_DIR=/app/uploads/images

EXPOSE 8080

# Render set bien PORT, fallback 8080 cho local
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
