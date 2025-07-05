FROM openjdk:24-jdk

WORKDIR /application

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]