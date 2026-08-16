package com.cloudoptimizer.backend.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;

import com.cloudoptimizer.backend.dto.SearchResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Service
public class QdrantService {

    private final QdrantClient qdrantClient;
    private final String collectionName;

    public QdrantService(
            QdrantClient qdrantClient,
            @Value("${qdrant.collection}") String collectionName
    ) {
        this.qdrantClient = qdrantClient;
        this.collectionName = collectionName;
    }

    /*
     * Automatically create the Qdrant collection
     * when Spring Boot starts.
     */
    @PostConstruct
    public void initializeQdrant() {
        createCollectionIfNotExists();
    }

    /*
     * Create collection if it doesn't already exist.
     */
    public void createCollectionIfNotExists() {

        try {

            boolean exists =
                    qdrantClient
                            .collectionExistsAsync(collectionName)
                            .get();

            if (!exists) {

                qdrantClient
                        .createCollectionAsync(
                                collectionName,
                                Collections.VectorParams.newBuilder()
                                        .setSize(768)
                                        .setDistance(
                                                Collections.Distance.Cosine
                                        )
                                        .build()
                        )
                        .get();

                System.out.println(
                        "Qdrant collection created: "
                                + collectionName
                );

            } else {

                System.out.println(
                        "Qdrant collection already exists: "
                                + collectionName
                );
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to create Qdrant collection",
                    e
            );
        }
    }

    /*
     * Store an embedding and its metadata in Qdrant.
     */
    public void storeEmbedding(
            float[] vector,
            String content,
            int chunkIndex,
            String filename
    ) {

        try {

            // Convert float[] to List<Float>
            List<Float> vectorList =
                    new ArrayList<>();

            for (float vectorValue : vector) {
                vectorList.add(vectorValue);
            }

            /*
             * Create Qdrant point.
             */
            Points.PointStruct point =
                    Points.PointStruct.newBuilder()

                            .setId(
                                    id(UUID.randomUUID())
                            )

                            .setVectors(
                                    vectors(vectorList)
                            )

                            /*
                             * Store chunkIndex as STRING.
                             *
                             * This avoids protobuf number
                             * compatibility problems.
                             */
                            .putAllPayload(
                                    Map.of(
                                            "content",
                                            value(content),

                                            "chunkIndex",
                                            value(
                                                    String.valueOf(
                                                            chunkIndex
                                                    )
                                            ),

                                            "filename",
                                            value(filename)
                                    )
                            )

                            .build();

            /*
             * Store point in Qdrant.
             */
            qdrantClient
                    .upsertAsync(
                            collectionName,
                            List.of(point)
                    )
                    .get();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to store embedding in Qdrant",
                    e
            );
        }
    }

    /*
     * Semantic search.
     *
     * Takes the embedding of the user's question
     * and returns the most relevant document chunks.
     */
    public List<SearchResult> searchSimilar(
            float[] queryVector,
            int limit
    ) {

        try {

            // Convert float[] to List<Float>
            List<Float> vectorList =
                    new ArrayList<>();

            for (float vectorValue : queryVector) {
                vectorList.add(vectorValue);
            }

            /*
             * Search Qdrant for vectors closest
             * to the user's query.
             */
            List<Points.ScoredPoint> results =
                    qdrantClient
                            .searchAsync(
                                    Points.SearchPoints.newBuilder()
                                            .setCollectionName(
                                                    collectionName
                                            )
                                            .addAllVector(
                                                    vectorList
                                            )
                                            .setLimit(limit)
                                            .setWithPayload(
                                                    Points.WithPayloadSelector
                                                            .newBuilder()
                                                            .setEnable(true)
                                                            .build()
                                            )
                                            .build()
                            )
                            .get();

            List<SearchResult> searchResults =
                    new ArrayList<>();

            /*
             * Process every Qdrant result.
             */
            for (Points.ScoredPoint result : results) {

                String content = "";

                String filename = "";

                int chunkIndex = -1;

                // -----------------------------
                // Get content
                // -----------------------------
                if (result.containsPayload("content")) {

                    content =
                            result
                                    .getPayloadOrThrow("content")
                                    .getStringValue();
                }

                // -----------------------------
                // Get filename
                // -----------------------------
                if (result.containsPayload("filename")) {

                    filename =
                            result
                                    .getPayloadOrThrow("filename")
                                    .getStringValue();
                }

                // -----------------------------
                // Get chunkIndex
                // -----------------------------
                if (result.containsPayload("chunkIndex")) {

                    try {

                        chunkIndex =
                                Integer.parseInt(
                                        result
                                                .getPayloadOrThrow(
                                                        "chunkIndex"
                                                )
                                                .getStringValue()
                                );

                    } catch (Exception e) {

                        chunkIndex = -1;
                    }
                }

                /*
                 * Create SearchResult.
                 */
                searchResults.add(
                        new SearchResult(
                                content,
                                result.getScore(),
                                chunkIndex,
                                filename
                        )
                );
            }

            return searchResults;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to search Qdrant",
                    e
            );
        }
    }
}