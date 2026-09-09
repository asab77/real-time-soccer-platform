FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S soccer && adduser -S soccer -G soccer
WORKDIR /app
COPY --from=build /workspace/target/soccer-platform-*.jar app.jar
USER soccer
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
