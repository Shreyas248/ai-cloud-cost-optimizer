package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.dto.SearchResult;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final RestClient restClient;

    public RagService(
            EmbeddingService embeddingService,
            QdrantService qdrantService
    ) {
        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;

        /*
         * Configure HTTP timeouts for Ollama.
         */
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        // Time allowed to connect to Ollama
        factory.setConnectTimeout(
                Duration.ofSeconds(10)
        );

        // Time allowed for Ollama to generate response
        factory.setReadTimeout(
                Duration.ofSeconds(120)
        );

        this.restClient =
                RestClient.builder()
                        .baseUrl("http://localhost:11434")
                        .requestFactory(factory)
                        .build();
    }

    public Map<String, Object> generateAnswer(
            String question
    ) {

        // =====================================================
        // 1. Convert question into embedding
        // =====================================================

        float[] queryVector =
                embeddingService.generateEmbedding(
                        question
                );


        // =====================================================
        // 2. Search Qdrant
        // =====================================================

        List<SearchResult> results =
                qdrantService.searchSimilar(
                        queryVector,
                        3
                );


        // =====================================================
        // 3. Build context from retrieved documents
        // =====================================================

        StringBuilder context =
                new StringBuilder();

        for (SearchResult result : results) {

            context.append(
                    result.getContent()
            );

            context.append("\n\n");
        }


        // =====================================================
        // 4. Build RAG prompt
        // =====================================================

        String prompt = """
                You are an AI Cloud Cost Optimization Assistant.

                Your job is to answer the user's question using
                ONLY the information provided in the Context.

                IMPORTANT RULES:

                1. Do not use outside knowledge.
                2. Do not invent AWS services, prices, savings,
                   percentages, or recommendations.
                3. If the answer is not present in the Context,
                   say that the provided documents do not contain
                   enough information to answer the question.
                4. Prefer specific information from the Context.
                5. Keep the answer concise and directly answer
                   the user's question.
                6. Do not mention that you are an AI model.
                7. Do not repeat the entire Context.

                Context:
                %s

                User Question:
                %s

                Answer:
                """.formatted(
                        context.toString(),
                        question
                );


        // =====================================================
        // 5. Prepare Ollama request
        // =====================================================

        Map<String, Object> request =
                new HashMap<>();

        request.put(
                "model",
                "llama3.2:3b"
        );

        request.put(
                "prompt",
                prompt
        );

        request.put(
                "stream",
                false
        );

        /*
         * Limit the generated response.
         *
         * This prevents Ollama from generating
         * extremely long answers.
         */
        Map<String, Object> options =
                new HashMap<>();

        options.put(
                "num_predict",
                200
        );

        request.put(
                "options",
                options
        );


        // =====================================================
        // 6. Call Ollama
        // =====================================================

        Map<String, Object> response =
                restClient
                        .post()
                        .uri("/api/generate")
                        .body(request)
                        .retrieve()
                        .body(Map.class);


        // =====================================================
        // 7. Extract generated answer
        // =====================================================

        String answer = "";

        if (response != null &&
                response.get("response") != null) {

            answer =
                    response
                            .get("response")
                            .toString()
                            .trim();
        }


        // =====================================================
        // 8. Build final response
        // =====================================================

        Map<String, Object> result =
                new HashMap<>();

        result.put(
                "query",
                question
        );

        result.put(
                "answer",
                answer
        );

        result.put(
                "sources",
                results
        );

        return result;
    }
}