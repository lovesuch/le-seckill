FROM openjdk:11
COPY target/myapp.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]