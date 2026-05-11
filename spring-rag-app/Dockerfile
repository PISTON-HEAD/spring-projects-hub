FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ── Download ONNX model files at BUILD time ────────────────────────────────────
# Downloaded once during image build and baked into the image.
# At runtime Spring AI reads from these local file:// URIs — no internet needed.
RUN mkdir -p /workspace/onnx-models && \
    curl -fSL -o /workspace/onnx-models/tokenizer.json \
    "https://raw.githubusercontent.com/spring-projects/spring-ai/main/models/spring-ai-transformers/src/main/resources/onnx/all-MiniLM-L6-v2/tokenizer.json" && \
    curl -fSL -o /workspace/onnx-models/model.onnx \
    "https://media.githubusercontent.com/media/spring-projects/spring-ai/refs/heads/main/models/spring-ai-transformers/src/main/resources/onnx/all-MiniLM-L6-v2/model.onnx"

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy ONNX model files from build stage — baked into the final image
COPY --from=build /workspace/onnx-models /app/onnx-models/

# Set /tmp as the Java temp dir.
# Set JVM memory limit suitable for Render free tier (512MB RAM).
# Set the ONNX model paths as system properties so OnnxEmbeddingModel reads local files.
ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/tmp -Xmx192m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC"

COPY --from=build /workspace/target/*.jar /app/app.jar

# Render injects PORT env var — Spring Boot reads server.port from it.
# Falls back to 8082 for local Docker runs.
EXPOSE 8082

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS \
    -Dserver.port=${PORT:-8082} \
    -Drag.onnx.tokenizer-uri=file:/app/onnx-models/tokenizer.json \
    -Drag.onnx.model-uri=file:/app/onnx-models/model.onnx \
 -jar /app/app.jar"]
    