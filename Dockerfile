FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw -B package -DskipTests --no-transfer-progress 2>/dev/null || \
    (apk add --no-cache maven && mvn -B package -DskipTests --no-transfer-progress)

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
