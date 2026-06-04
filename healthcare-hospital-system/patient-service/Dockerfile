FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml pom.xml
RUN --mount=type=cache,target=/root/.m2 \
 mvn -B dependency:resolve dependency:resolve-plugins
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
 mvn -B clean package


FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8481
EXPOSE 9091
ENTRYPOINT [ "java", "-jar", "app.jar" ]
