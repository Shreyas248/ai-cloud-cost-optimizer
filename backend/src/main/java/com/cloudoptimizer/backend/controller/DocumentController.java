package com.cloudoptimizer.backend.controller;

import com.cloudoptimizer.backend.model.DocumentChunk;
import com.cloudoptimizer.backend.service.DocumentExtractionService;
import com.cloudoptimizer.backend.service.EmbeddingService;
import com.cloudoptimizer.backend.service.TextChunkerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    private final Path uploadDirectory;

    private final DocumentExtractionService extractionService;

    private final TextChunkerService textChunkerService;

    private final EmbeddingService embeddingService;


    public DocumentController(
            DocumentExtractionService extractionService,
            TextChunkerService textChunkerService,
            EmbeddingService embeddingService
    ) {

        this.extractionService =
                extractionService;

        this.textChunkerService =
                textChunkerService;

        this.embeddingService =
                embeddingService;


        this.uploadDirectory =
                Paths.get("uploads")
                        .toAbsolutePath()
                        .normalize();


        try {

            Files.createDirectories(
                    uploadDirectory
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not create upload directory",
                    e
            );
        }
    }


    // =========================================================
    // 1. UPLOAD DOCUMENT
    // =========================================================

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Please select a file"
                    ));
        }


        String originalFilename =
                file.getOriginalFilename();


        if (originalFilename == null ||
                originalFilename.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Invalid filename"
                    ));
        }


        String filename =
                Paths.get(originalFilename)
                        .getFileName()
                        .toString();


        String extension = "";

        int lastDot =
                filename.lastIndexOf(".");


        if (lastDot > 0) {

            extension =
                    filename
                            .substring(lastDot + 1)
                            .toLowerCase();
        }


        if (!extension.equals("pdf") &&
                !extension.equals("csv")) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNSUPPORTED_MEDIA_TYPE
                    )
                    .body(Map.of(
                            "error",
                            "Only PDF and CSV files are supported"
                    ));
        }


        try {

            Path targetLocation =
                    uploadDirectory
                            .resolve(filename)
                            .normalize();


            if (!targetLocation
                    .getParent()
                    .equals(uploadDirectory)) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "error",
                                "Invalid filename"
                        ));
            }


            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );


            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "File uploaded successfully",

                            "filename",
                            filename,

                            "type",
                            extension,

                            "size",
                            file.getSize()
                    )
            );


        } catch (IOException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "error",
                            "Failed to save file"
                    ));
        }
    }


    // =========================================================
    // 2. EXTRACT TEXT
    // =========================================================

    @PostMapping("/extract")
    public ResponseEntity<?> extractDocument(
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Please select a file"
                    ));
        }


        try {

            String content =
                    extractionService.extractText(file);


            return ResponseEntity.ok(
                    Map.of(
                            "filename",
                            file.getOriginalFilename(),

                            "size",
                            file.getSize(),

                            "content",
                            content
                    )
            );


        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));


        } catch (IOException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "error",
                            "Failed to extract document"
                    ));
        }
    }


    // =========================================================
    // 3. CHUNK DOCUMENT
    // =========================================================

    @PostMapping("/chunk")
    public ResponseEntity<?> chunkDocument(
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Please select a file"
                    ));
        }


        try {

            String content =
                    extractionService.extractText(file);


            List<DocumentChunk> chunks =
                    textChunkerService.chunkText(
                            content
                    );


            return ResponseEntity.ok(
                    Map.of(
                            "filename",
                            file.getOriginalFilename(),

                            "chunkCount",
                            chunks.size(),

                            "chunks",
                            chunks
                    )
            );


        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));


        } catch (IOException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "error",
                            "Failed to process document"
                    ));
        }
    }


    // =========================================================
    // 4. GENERATE EMBEDDINGS
    // =========================================================

    @PostMapping("/embed")
    public ResponseEntity<?> embedDocument(
            @RequestParam("file") MultipartFile file
    ) {

        if (file.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Please select a file"
                    ));
        }


        try {

            // Step 1: Extract text
            String content =
                    extractionService.extractText(file);


            // Step 2: Split into chunks
            List<DocumentChunk> chunks =
                    textChunkerService.chunkText(
                            content
                    );


            // Step 3: Generate embeddings
            List<Map<String, Object>> embeddings =
                    new ArrayList<>();


            for (DocumentChunk chunk : chunks) {

                float[] vector =
                        embeddingService.generateEmbedding(
                                chunk.getContent()
                        );


                embeddings.add(
                        Map.of(
                                "chunkIndex",
                                chunk.getChunkIndex(),

                                "content",
                                chunk.getContent(),

                                "vectorDimensions",
                                vector.length
                        )
                );
            }


            // Step 4: Return results
            return ResponseEntity.ok(
                    Map.of(
                            "filename",
                            file.getOriginalFilename(),

                            "chunkCount",
                            chunks.size(),

                            "embeddings",
                            embeddings
                    )
            );


        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            e.getMessage()
                    ));


        } catch (IOException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(Map.of(
                            "error",
                            "Failed to process document"
                    ));
        }
    }
}