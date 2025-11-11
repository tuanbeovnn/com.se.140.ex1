# Use an official Eclipse Temurin runtime as a parent image
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
COPY src ./src

RUN ./mvnw clean package -DskipTests
RUN cp target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
