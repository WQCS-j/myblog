FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY backend-java/pom.xml .
COPY backend-java/src ./src
RUN mvn package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java","-jar","app.jar"]
