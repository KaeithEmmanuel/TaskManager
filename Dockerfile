# build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/TaskManager-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE 8150
ENTRYPOINT ["java","-jar","/app/app.jar"]
