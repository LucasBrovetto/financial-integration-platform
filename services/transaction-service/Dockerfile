FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
    && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src src
RUN ./mvnw --batch-mode --no-transfer-progress package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring \
    && adduser -S spring -G spring

WORKDIR /app

COPY --from=build /workspace/target/transaction-service-0.0.1-SNAPSHOT.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
