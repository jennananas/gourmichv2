# Étape 1 : build avec Maven + JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app/gourmich-back
COPY gourmich-back/ .
RUN mvn clean package -DskipTests

# Étape 2 : image finale avec JRE 17
FROM eclipse-temurin:17-jre
WORKDIR /app/gourmich-back
COPY --from=builder /app/gourmich-back/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]