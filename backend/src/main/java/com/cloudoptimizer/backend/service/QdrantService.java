package com.cloudoptimizer.backend.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public void storeEmbedding(
            float[] vector,
            String content,
            int chunkIndex,
            String filename
    ) {

        try {

            // Convert float[] to List<Float>
            List<Float> vectorList =
                    new java.util.ArrayList<>();

            for (float value : vector) {
                vectorList.add(value);
            }

            // Create Qdrant point
            Points.PointStruct point =
                    Points.PointStruct.newBuilder()

                            .setId(
                                    id(UUID.randomUUID())
                            )

                            .setVectors(
                                    vectors(vectorList)
                            )

                            .putAllPayload(
                                    Map.of(
                                            "content",
                                            value(content),

                                            "chunkIndex",
                                            value(chunkIndex),

                                            "filename",
                                            value(filename)
                                    )
                            )

                            .build();

            // Store point in Qdrant
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
}