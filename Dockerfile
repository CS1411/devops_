FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/status-monitor-1.0.jar app.jar
RUN useradd --system --uid 1001 appuser
USER appuser
ENV SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
