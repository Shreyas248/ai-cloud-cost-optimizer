package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.dto.SearchResult;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Points;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Service
public class QdrantService {

    private final QdrantClient qdrantClient;

    private final String collectionName;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public QdrantService(
            QdrantClient qdrantClient,
            @Value("${qdrant.collection}") String collectionName
    ) {

        this.qdrantClient = qdrantClient;
        this.collectionName = collectionName;

        System.out.println(
                "================================="
        );

        System.out.println(
                "QdrantService initialized"
        );

        System.out.println(
                "Qdrant collection: "
                        + collectionName
        );

        System.out.println(
                "================================="
        );
    }


    // =========================================================
    // INITIALIZE QDRANT
    // =========================================================

    @PostConstruct
    public void initializeQdrant() {

        System.out.println(
                "Initializing Qdrant..."
        );

        createCollectionIfNotExists();
    }


    // =========================================================
    // CREATE COLLECTION IF NOT EXISTS
    // =========================================================

    public void createCollectionIfNotExists() {

        try {

            System.out.println(
                    "Checking Qdrant collection: "
                            + collectionName
            );


            boolean exists =
                    qdrantClient
                            .collectionExistsAsync(
                                    collectionName
                            )
                            .get();


            if (!exists) {

                System.out.println(
                        "Collection does not exist."
                );

                System.out.println(
                        "Creating collection: "
                                + collectionName
                );


                qdrantClient
                        .createCollectionAsync(
                                collectionName,

                                Collections.VectorParams
                                        .newBuilder()

                                        .setSize(768)

                                        .setDistance(
                                                Collections.Distance.Cosine
                                        )

                                        .build()
                        )
                        .get();


                System.out.println(
                        "================================="
                );

                System.out.println(
                        "QDRANT COLLECTION CREATED"
                );

                System.out.println(
                        "Collection: "
                                + collectionName
                );

                System.out.println(
                        "Vector size: 768"
                );

                System.out.println(
                        "Distance: Cosine"
                );

                System.out.println(
                        "================================="
                );

            } else {

                System.out.println(
                        "Qdrant collection already exists: "
                                + collectionName
                );
            }


        } catch (Exception e) {

            System.err.println(
                    "================================="
            );

            System.err.println(
                    "QDRANT COLLECTION CREATION FAILED"
            );

            System.err.println(
                    "Collection: "
                            + collectionName
            );

            System.err.println(
                    "Error: "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.err.println(
                    "================================="
            );


            throw new RuntimeException(
                    "Failed to create Qdrant collection",
                    e
            );
        }
    }


    // =========================================================
    // STORE EMBEDDING
    //
    // IMPORTANT:
    //
    // userId is stored as a STRING.
    //
    // Example:
    //
    // User A:
    // userId = "1"
    //
    // User B:
    // userId = "2"
    //
    // This allows us to use matchKeyword().
    // =========================================================

    public void storeEmbedding(
            float[] vector,
            String content,
            int chunkIndex,
            String filename,
            Long userId
    ) {

        try {

            // =================================================
            // VALIDATE USER ID
            // =================================================

            if (userId == null) {

                throw new IllegalArgumentException(
                        "User ID cannot be null"
                );
            }


            // =================================================
            // VALIDATE VECTOR
            // =================================================

            if (
                    vector == null ||
                    vector.length == 0
            ) {

                throw new IllegalArgumentException(
                        "Embedding vector cannot be empty"
                );
            }


            // =================================================
            // CHECK VECTOR DIMENSION
            // =================================================

            if (vector.length != 768) {

                throw new IllegalArgumentException(
                        "Invalid embedding dimension: "
                                + vector.length
                                + ". Expected 768."
                );
            }


            // =================================================
            // CONVERT float[] → List<Float>
            // =================================================

            List<Float> vectorList =
                    new ArrayList<>();


            for (float vectorValue : vector) {

                vectorList.add(
                        vectorValue
                );
            }


            // =================================================
            // CONVERT USER ID TO STRING
            // =================================================

            String userIdString =
                    String.valueOf(userId);


            // =================================================
            // CREATE QDRANT POINT
            // =================================================

            Points.PointStruct point =
                    Points.PointStruct
                            .newBuilder()

                            // =================================
                            // UNIQUE POINT ID
                            // =================================

                            .setId(
                                    id(
                                            UUID.randomUUID()
                                    )
                            )

                            // =================================
                            // VECTOR
                            // =================================

                            .setVectors(
                                    vectors(
                                            vectorList
                                    )
                            )

                            // =================================
                            // PAYLOAD
                            // =================================

                            .putAllPayload(
                                    Map.of(

                                            // -----------------
                                            // USER ID
                                            // -----------------
                                            //
                                            // IMPORTANT:
                                            // Stored as STRING
                                            //
                                            // Example:
                                            // "1"
                                            // "2"
                                            //
                                            "userId",
                                            value(
                                                    userIdString
                                            ),


                                            // -----------------
                                            // DOCUMENT CONTENT
                                            // -----------------

                                            "content",
                                            value(
                                                    content
                                            ),


                                            // -----------------
                                            // CHUNK INDEX
                                            // -----------------

                                            "chunkIndex",
                                            value(
                                                    String.valueOf(
                                                            chunkIndex
                                                    )
                                            ),


                                            // -----------------
                                            // FILENAME
                                            // -----------------

                                            "filename",
                                            value(
                                                    filename
                                            )
                                    )
                            )

                            .build();


            // =================================================
            // INSERT POINT INTO QDRANT
            // =================================================

            qdrantClient
                    .upsertAsync(
                            collectionName,
                            List.of(point)
                    )
                    .get();


            // =================================================
            // LOG
            // =================================================

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "QDRANT VECTOR STORED"
            );

            System.out.println(
                    "userId = "
                            + userIdString
            );

            System.out.println(
                    "filename = "
                            + filename
            );

            System.out.println(
                    "chunk = "
                            + chunkIndex
            );

            System.out.println(
                    "================================="
            );


        } catch (Exception e) {

            System.err.println(
                    "================================="
            );

            System.err.println(
                    "FAILED TO STORE EMBEDDING"
            );

            System.err.println(
                    "userId = "
                            + userId
            );

            System.err.println(
                    "filename = "
                            + filename
            );

            System.err.println(
                    "chunk = "
                            + chunkIndex
            );

            System.err.println(
                    "Error = "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.err.println(
                    "================================="
            );


            throw new RuntimeException(
                    "Failed to store embedding in Qdrant",
                    e
            );
        }
    }


    // =========================================================
    // SEARCH SIMILAR
    //
    // THIS IS THE IMPORTANT PART.
    //
    // Qdrant searches ONLY points belonging to the
    // authenticated user's ID.
    // =========================================================

    public List<SearchResult> searchSimilar(
            float[] queryVector,
            int limit,
            Long userId
    ) {

        try {

            // =================================================
            // VALIDATE USER ID
            // =================================================

            if (userId == null) {

                throw new IllegalArgumentException(
                        "User ID cannot be null"
                );
            }


            // =================================================
            // VALIDATE QUERY VECTOR
            // =================================================

            if (
                    queryVector == null ||
                    queryVector.length == 0
            ) {

                throw new IllegalArgumentException(
                        "Query vector cannot be empty"
                );
            }


            // =================================================
            // CHECK VECTOR DIMENSION
            // =================================================

            if (queryVector.length != 768) {

                throw new IllegalArgumentException(
                        "Invalid query vector dimension: "
                                + queryVector.length
                                + ". Expected 768."
                );
            }


            // =================================================
            // CONVERT QUERY VECTOR
            // =================================================

            List<Float> vectorList =
                    new ArrayList<>();


            for (float vectorValue : queryVector) {

                vectorList.add(
                        vectorValue
                );
            }


            // =================================================
            // CONVERT USER ID TO STRING
            // =================================================

            String userIdString =
                    String.valueOf(userId);


            // =================================================
            // CREATE USER FILTER
            // =================================================
            //
            // This means:
            //
            // ONLY return Qdrant points where:
            //
            // userId == current user's ID
            //
            // Example:
            //
            // User A → "1"
            //
            // User B → "2"
            //
            // User B searches:
            //
            // userId == "2"
            //
            // Therefore User A's vectors ("1")
            // will NOT be returned.
            //
            // =================================================

            Filter userFilter =
                    Filter
                            .newBuilder()
                            .addMust(
                                    matchKeyword(
                                            "userId",
                                            userIdString
                                    )
                            )
                            .build();


            // =================================================
            // SEARCH QDRANT
            // =================================================

            List<Points.ScoredPoint> results =
                    qdrantClient
                            .searchAsync(

                                    Points.SearchPoints
                                            .newBuilder()

                                            // -----------------
                                            // COLLECTION
                                            // -----------------

                                            .setCollectionName(
                                                    collectionName
                                            )

                                            // -----------------
                                            // QUERY VECTOR
                                            // -----------------

                                            .addAllVector(
                                                    vectorList
                                            )

                                            // -----------------
                                            // USER FILTER
                                            // -----------------

                                            .setFilter(
                                                    userFilter
                                            )

                                            // -----------------
                                            // RESULT LIMIT
                                            // -----------------

                                            .setLimit(
                                                    limit
                                            )

                                            // -----------------
                                            // RETURN PAYLOAD
                                            // -----------------

                                            .setWithPayload(
                                                    Points
                                                            .WithPayloadSelector
                                                            .newBuilder()
                                                            .setEnable(
                                                                    true
                                                            )
                                                            .build()
                                            )

                                            .build()

                            )
                            .get();


            // =================================================
            // LOG SEARCH
            // =================================================

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "QDRANT SEARCH"
            );

            System.out.println(
                    "userId = "
                            + userIdString
            );

            System.out.println(
                    "results = "
                            + results.size()
            );

            System.out.println(
                    "================================="
            );


            // =================================================
            // CREATE SEARCH RESULTS
            // =================================================

            List<SearchResult> searchResults =
                    new ArrayList<>();


            // =================================================
            // PROCESS RESULTS
            // =================================================

            for (
                    Points.ScoredPoint result :
                    results
            ) {

                String content = "";

                String filename = "";

                int chunkIndex = -1;


                // =============================================
                // CONTENT
                // =============================================

                if (
                        result.containsPayload(
                                "content"
                        )
                ) {

                    content =
                            result
                                    .getPayloadOrThrow(
                                            "content"
                                    )
                                    .getStringValue();
                }


                // =============================================
                // FILENAME
                // =============================================

                if (
                        result.containsPayload(
                                "filename"
                        )
                ) {

                    filename =
                            result
                                    .getPayloadOrThrow(
                                            "filename"
                                    )
                                    .getStringValue();
                }


                // =============================================
                // CHUNK INDEX
                // =============================================

                if (
                        result.containsPayload(
                                "chunkIndex"
                        )
                ) {

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


                // =============================================
                // CREATE SearchResult
                // =============================================

                searchResults.add(
                        new SearchResult(
                                content,
                                result.getScore(),
                                chunkIndex,
                                filename
                        )
                );
            }


            // =================================================
            // RETURN RESULTS
            // =================================================

            return searchResults;


        } catch (Exception e) {

            System.err.println(
                    "================================="
            );

            System.err.println(
                    "QDRANT SEARCH FAILED"
            );

            System.err.println(
                    "userId = "
                            + userId
            );

            System.err.println(
                    "Error = "
                            + e.getMessage()
            );

            e.printStackTrace();

            System.err.println(
                    "================================="
            );


            throw new RuntimeException(
                    "Failed to search Qdrant",
                    e
            );
        }
    }
}