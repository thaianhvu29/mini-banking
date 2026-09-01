FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/audit-service-0.0.1-SNAPSHOT.jar app.jar

RUN groupadd --system spring \
    && useradd --system --gid spring spring \
    && chown spring:spring app.jar

USER spring

ENTRYPOINT ["java", "-jar", "app.jar"]
