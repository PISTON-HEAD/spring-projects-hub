FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml pom.xml
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package:DskipTests


