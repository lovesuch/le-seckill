FROM openjdk:11
COPY target/your-application.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]