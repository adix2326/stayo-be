# Build Stage
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/target/StayO-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -jar app.jar --server.port=${PORT:-8080}"]