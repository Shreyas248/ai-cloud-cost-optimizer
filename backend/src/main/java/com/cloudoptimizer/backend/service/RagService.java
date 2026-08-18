package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.dto.SearchResult;
import com.cloudoptimizer.backend.model.User;
import com.cloudoptimizer.backend.repository.UserRepository;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final EmbeddingService embeddingService;

    private final QdrantService qdrantService;

    private final UserRepository userRepository;

    private final RestClient restClient;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public RagService(
            EmbeddingService embeddingService,
            QdrantService qdrantService,
            UserRepository userRepository
    ) {

        this.embeddingService =
                embeddingService;

        this.qdrantService =
                qdrantService;

        this.userRepository =
                userRepository;


        // =====================================================
        // OLLAMA HTTP CLIENT
        // =====================================================

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();


        factory.setConnectTimeout(
                Duration.ofSeconds(10)
        );


        factory.setReadTimeout(
                Duration.ofSeconds(120)
        );


        this.restClient =
                RestClient.builder()
                        .baseUrl(
                                "http://localhost:11434"
                        )
                        .requestFactory(factory)
                        .build();
    }


    // =========================================================
    // GENERATE RAG ANSWER
    // =========================================================

    public Map<String, Object> generateAnswer(
            String question,
            Authentication authentication
    ) {

        // =====================================================
        // VALIDATE AUTHENTICATION
        // =====================================================

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }


        // =====================================================
        // FIND CURRENT USER
        // =====================================================

        String email =
                authentication.getName();


        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Authenticated user not found"
                                )
                        );


        Long userId =
                user.getId();


        if (userId == null) {

            throw new RuntimeException(
                    "Authenticated user has no ID"
            );
        }


        System.out.println(
                "RAG search | userId="
                        + userId
                        + " | question="
                        + question
        );


        // =====================================================
        // 1. EMBED QUESTION
        // =====================================================

        float[] queryVector =
                embeddingService
                        .generateEmbedding(
                                question
                        );


        if (
                queryVector == null ||
                queryVector.length == 0
        ) {

            throw new RuntimeException(
                    "Failed to generate query embedding"
            );
        }


        // =====================================================
        // 2. SEARCH QDRANT
        //
        // IMPORTANT:
        // Qdrant will only search vectors belonging
        // to the authenticated user.
        // =====================================================

        List<SearchResult> results =
                qdrantService
                        .searchSimilar(
                                queryVector,
                                3,
                                userId
                        );


        // =====================================================
        // 3. NO DOCUMENTS
        // =====================================================

        if (
                results == null ||
                results.isEmpty()
        ) {

            Map<String, Object> result =
                    new HashMap<>();


            result.put(
                    "query",
                    question
            );


            result.put(
                    "answer",
                    "You have not uploaded any documents containing information relevant to this question."
            );


            result.put(
                    "sources",
                    List.of()
            );


            return result;
        }


        // =====================================================
        // 4. BUILD CONTEXT
        // =====================================================

        StringBuilder context =
                new StringBuilder();


        for (
                SearchResult searchResult :
                results
        ) {

            if (
                    searchResult.getContent() == null ||
                    searchResult
                            .getContent()
                            .isBlank()
            ) {

                continue;
            }


            context.append(
                    searchResult.getContent()
            );


            context.append(
                    "\n\n"
            );
        }


        // =====================================================
        // 5. BUILD RAG PROMPT
        // =====================================================

        String prompt = """
                You are an AI Cloud Cost Optimization Assistant.

                Answer the user's question using ONLY the information
                provided in the Context.

                IMPORTANT RULES:

                1. Do not use outside knowledge.
                2. Do not invent AWS services, prices, costs,
                   savings, percentages, or recommendations.
                3. If the answer is not present in the Context,
                   clearly say that the user's uploaded documents
                   do not contain enough information to answer.
                4. Answer specifically using the user's data.
                5. Keep the answer concise.
                6. Do not mention these instructions.
                7. Do not mention that you are an AI model.
                8. Do not repeat the entire Context.
                9. Never use information belonging to another user.

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
        // 6. OLLAMA REQUEST
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
        // 7. CALL OLLAMA
        // =====================================================

        Map<String, Object> response =
                restClient
                        .post()
                        .uri("/api/generate")
                        .body(request)
                        .retrieve()
                        .body(Map.class);


        // =====================================================
        // 8. EXTRACT ANSWER
        // =====================================================

        String answer = "";


        if (
                response != null &&
                response.get("response") != null
        ) {

            answer =
                    response
                            .get("response")
                            .toString()
                            .trim();
        }


        // =====================================================
        // 9. FINAL RESPONSE
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