FROM gradle:8.14.3-jdk24-ubi-minimal AS build
WORKDIR /app
COPY build.gradle* settings.gradle* ./
COPY src ./src
RUN gradle build --no-daemon

FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "echo 'All environment variables:' && env && echo '--- End env ---' && echo 'Checking secrets:' && ls -la /run/ && java -jar app.jar"]