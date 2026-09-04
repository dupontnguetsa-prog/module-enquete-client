FROM gradle:9.7.1-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/module-enquete-client-1.0.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
