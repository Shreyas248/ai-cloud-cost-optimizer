// package com.cloudoptimizer.backend.service;

// import org.springframework.ai.embedding.EmbeddingModel;
// import org.springframework.stereotype.Service;

// @Service
// public class EmbeddingService {

//     private final EmbeddingModel embeddingModel;

//     public EmbeddingService(
//             EmbeddingModel embeddingModel
//     ) {
//         this.embeddingModel = embeddingModel;
//     }

//     public float[] generateEmbedding(
//             String text
//     ) {

//         if (text == null || text.isBlank()) {
//             throw new IllegalArgumentException(
//                     "Text cannot be empty"
//             );
//         }

//         return embeddingModel
//                 .embed(text);
//     }
// }

package com.cloudoptimizer.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestClient restClient;

    public EmbeddingService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

    @SuppressWarnings("unchecked")
    public float[] generateEmbedding(String text) {

        Map<String, Object> request = Map.of(
                "model", "nomic-embed-text:latest",
                "input", text
        );

        Map<String, Object> response = restClient.post()
                .uri("/api/embed")
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("embeddings") == null) {
            throw new RuntimeException(
                    "Ollama did not return an embedding"
            );
        }

        List<List<Double>> embeddings =
                (List<List<Double>>) response.get("embeddings");

        List<Double> embedding = embeddings.get(0);

        float[] vector = new float[embedding.size()];

        for (int i = 0; i < embedding.size(); i++) {
            vector[i] = embedding.get(i).floatValue();
        }

        return vector;
    }
}