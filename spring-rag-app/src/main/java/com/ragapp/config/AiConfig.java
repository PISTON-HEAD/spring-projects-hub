package com.ragapp.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(OnnxEmbeddingModel onnxEmbeddingModel) throws Exception {
        onnxEmbeddingModel.afterPropertiesSet();
        return onnxEmbeddingModel;
    }

    @Bean
    public OnnxEmbeddingModel onnxEmbeddingModel() {
        return new OnnxEmbeddingModel();
    }

    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
