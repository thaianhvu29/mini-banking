FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/notification-service-0.0.1-SNAPSHOT.jar app.jar

USER 1000

ENTRYPOINT ["java", "-jar", "app.jar"]
