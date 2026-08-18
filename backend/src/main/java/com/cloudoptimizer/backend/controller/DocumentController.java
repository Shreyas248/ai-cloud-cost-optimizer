package com.cloudoptimizer.backend.controller;

import com.cloudoptimizer.backend.model.DocumentChunk;
import com.cloudoptimizer.backend.model.User;
import com.cloudoptimizer.backend.repository.UserRepository;
import com.cloudoptimizer.backend.service.CloudCostImportService;
import com.cloudoptimizer.backend.service.DocumentExtractionService;
import com.cloudoptimizer.backend.service.EmbeddingService;
import com.cloudoptimizer.backend.service.QdrantService;
import com.cloudoptimizer.backend.service.TextChunkerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final QdrantService qdrantService;
    private final CloudCostImportService cloudCostImportService;
    private final UserRepository userRepository;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public DocumentController(
            DocumentExtractionService extractionService,
            TextChunkerService textChunkerService,
            EmbeddingService embeddingService,
            QdrantService qdrantService,
            CloudCostImportService cloudCostImportService,
            UserRepository userRepository
    ) {

        this.extractionService = extractionService;
        this.textChunkerService = textChunkerService;
        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;
        this.cloudCostImportService = cloudCostImportService;
        this.userRepository = userRepository;

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
    // GET AUTHENTICATED USER
    // =========================================================

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        // =====================================================
        // CHECK AUTHENTICATION
        // =====================================================

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {

            throw new SecurityException(
                    "User is not authenticated"
            );
        }

        // =====================================================
        // GET PRINCIPAL
        // =====================================================

        Object principal =
                authentication.getPrincipal();

        String email = null;

        // =====================================================
        // PRINCIPAL IS USER
        // =====================================================

        if (principal instanceof User user) {

            email = user.getEmail();
        }

        // =====================================================
        // PRINCIPAL IS STRING
        // =====================================================

        else if (principal instanceof String principalString) {

            email = principalString;
        }

        // =====================================================
        // FALLBACK TO AUTHENTICATION NAME
        // =====================================================

        if (
                email == null ||
                email.isBlank()
        ) {

            email =
                    authentication.getName();
        }

        // =====================================================
        // VALIDATE EMAIL
        // =====================================================

        if (
                email == null ||
                email.isBlank()
        ) {

            throw new SecurityException(
                    "Authenticated user email is missing"
            );
        }

        email =
                email
                        .trim()
                        .toLowerCase();

        System.out.println("=================================");
        System.out.println("AUTHENTICATED USER");
        System.out.println("Email = " + email);
        System.out.println(
                "Principal type = "
                        + principal.getClass().getName()
        );
        System.out.println("=================================");

        // =====================================================
        // FIND USER IN POSTGRESQL
        // =====================================================

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        // =====================================================
        // USER NOT FOUND
        // =====================================================

        if (user == null) {

            System.err.println("=================================");
            System.err.println(
                    "AUTHENTICATED USER NOT FOUND"
            );
            System.err.println(
                    "Email = " + email
            );
            System.err.println(
                    "User does not exist in PostgreSQL"
            );
            System.err.println("=================================");

            throw new SecurityException(
                    "Authenticated user not found. "
                            + "Please login again."
            );
        }

        // =====================================================
        // VALIDATE USER ID
        // =====================================================

        if (user.getId() == null) {

            throw new SecurityException(
                    "Authenticated user has no database ID"
            );
        }

        System.out.println(
                "User found successfully"
        );

        System.out.println(
                "User ID = "
                        + user.getId()
        );

        System.out.println(
                "User Email = "
                        + user.getEmail()
        );

        return user;
    }

    // =========================================================
    // UPLOAD DOCUMENT
    // =========================================================

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        if (
                file == null ||
                file.isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Please select a file"
                            )
                    );
        }

        String originalFilename =
                file.getOriginalFilename();

        if (
                originalFilename == null ||
                originalFilename.isBlank()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Invalid filename"
                            )
                    );
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

        // =====================================================
        // ONLY PDF AND CSV
        // =====================================================

        if (
                !extension.equals("pdf") &&
                !extension.equals("csv")
        ) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNSUPPORTED_MEDIA_TYPE
                    )
                    .body(
                            Map.of(
                                    "error",
                                    "Only PDF and CSV files are supported"
                            )
                    );
        }

        try {

            // =================================================
            // GET USER
            // =================================================

            User user =
                    getAuthenticatedUser(
                            authentication
                    );

            Long userId =
                    user.getId();

            // =================================================
            // SAVE FILE
            // =================================================

            Path targetLocation =
                    uploadDirectory
                            .resolve(filename)
                            .normalize();

            if (
                    targetLocation.getParent() == null ||
                    !targetLocation
                            .getParent()
                            .equals(uploadDirectory)
            ) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "Invalid filename"
                                )
                        );
            }

            Files.copy(
                    file.getInputStream(),
                    targetLocation,
                    java.nio.file.StandardCopyOption
                            .REPLACE_EXISTING
            );

            System.out.println(
                    "File saved: "
                            + targetLocation
            );

            // =================================================
            // EXTRACT TEXT
            // =================================================

            String content =
                    extractionService
                            .extractText(file);

            if (
                    content == null ||
                    content.isBlank()
            ) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "The uploaded document contains no readable text"
                                )
                        );
            }

            // =================================================
            // IMPORT CSV
            // =================================================

            int importedRecords = 0;

            if (extension.equals("csv")) {

                importedRecords =
                        cloudCostImportService
                                .importCsv(
                                        file,
                                        user
                                );

                System.out.println(
                        "CSV imported records = "
                                + importedRecords
                );
            }

            // =================================================
            // INDEX INTO QDRANT
            // =================================================

            int indexedChunks =
                    indexDocument(
                            content,
                            filename,
                            userId
                    );

            // =================================================
            // SUCCESS
            // =================================================

            return ResponseEntity.ok(
                    Map.of(

                            "message",
                            extension.equals("csv")
                                    ? "File uploaded, cloud costs imported, and RAG indexing completed successfully"
                                    : "File uploaded and RAG indexing completed successfully",

                            "filename",
                            filename,

                            "type",
                            extension,

                            "size",
                            file.getSize(),

                            "userId",
                            userId,

                            "importedRecords",
                            importedRecords,

                            "indexedChunks",
                            indexedChunks
                    )
            );

        } catch (SecurityException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "error",
                                    "Failed to save or process file"
                            )
                    );

        } catch (RuntimeException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "File processing failed"
                            )
                    );
        }
    }

    // =========================================================
    // INDEX DOCUMENT INTO QDRANT
    // =========================================================

    private int indexDocument(
            String content,
            String filename,
            Long userId
    ) {

        List<DocumentChunk> chunks =
                textChunkerService
                        .chunkText(content);

        if (
                chunks == null ||
                chunks.isEmpty()
        ) {

            throw new RuntimeException(
                    "No chunks were generated from the document"
            );
        }

        System.out.println(
                "Generated chunks = "
                        + chunks.size()
        );

        int indexedChunks = 0;

        for (
                DocumentChunk chunk :
                chunks
        ) {

            if (
                    chunk.getContent() == null ||
                    chunk.getContent().isBlank()
            ) {

                continue;
            }

            System.out.println(
                    "Processing chunk "
                            + chunk.getChunkIndex()
            );

            // =================================================
            // GENERATE EMBEDDING
            // =================================================

            float[] vector =
                    embeddingService
                            .generateEmbedding(
                                    chunk.getContent()
                            );

            if (
                    vector == null ||
                    vector.length == 0
            ) {

                throw new RuntimeException(
                        "Ollama returned an empty embedding"
                );
            }

            // =================================================
            // CHECK VECTOR DIMENSION
            // =================================================

            if (vector.length != 768) {

                throw new RuntimeException(
                        "Invalid embedding dimension: "
                                + vector.length
                                + ". Expected 768."
                );
            }

            System.out.println(
                    "Embedding generated | dimension="
                            + vector.length
            );

            // =================================================
            // STORE IN QDRANT
            // =================================================

            qdrantService.storeEmbedding(
                    vector,
                    chunk.getContent(),
                    chunk.getChunkIndex(),
                    filename,
                    userId
            );

            indexedChunks++;
        }

        if (indexedChunks == 0) {

            throw new RuntimeException(
                    "No valid document chunks were indexed"
            );
        }

        System.out.println("=================================");
        System.out.println("RAG INDEXING COMPLETED");
        System.out.println(
                "userId = "
                        + userId
        );
        System.out.println(
                "filename = "
                        + filename
        );
        System.out.println(
                "chunks = "
                        + indexedChunks
        );
        System.out.println("=================================");

        return indexedChunks;
    }

    // =========================================================
    // EXTRACT DOCUMENT
    // =========================================================

    @PostMapping("/extract")
    public ResponseEntity<?> extractDocument(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        if (
                file == null ||
                file.isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Please select a file"
                            )
                    );
        }

        try {

            String content =
                    extractionService
                            .extractText(file);

            String filename =
                    file.getOriginalFilename();

            int importedRecords = 0;

            if (
                    filename != null &&
                    filename
                            .toLowerCase()
                            .endsWith(".csv")
            ) {

                User user =
                        getAuthenticatedUser(
                                authentication
                        );

                importedRecords =
                        cloudCostImportService
                                .importCsv(
                                        file,
                                        user
                                );
            }

            return ResponseEntity.ok(
                    Map.of(

                            "filename",
                            filename,

                            "size",
                            file.getSize(),

                            "content",
                            content,

                            "importedRecords",
                            importedRecords
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (IOException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "error",
                                    "Failed to extract document"
                            )
                    );

        } catch (RuntimeException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Document extraction failed"
                            )
                    );
        }
    }

    // =========================================================
    // CHUNK DOCUMENT
    // =========================================================

    @PostMapping("/chunk")
    public ResponseEntity<?> chunkDocument(
            @RequestParam("file") MultipartFile file
    ) {

        if (
                file == null ||
                file.isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Please select a file"
                            )
                    );
        }

        try {

            String content =
                    extractionService
                            .extractText(file);

            List<DocumentChunk> chunks =
                    textChunkerService
                            .chunkText(content);

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
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (IOException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "error",
                                    "Failed to process document"
                            )
                    );
        }
    }

    // =========================================================
    // EMBED DOCUMENT
    // =========================================================

    @PostMapping("/embed")
    public ResponseEntity<?> embedDocument(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        if (
                file == null ||
                file.isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Please select a file"
                            )
                    );
        }

        try {

            // =================================================
            // GET USER
            // =================================================

            User user =
                    getAuthenticatedUser(
                            authentication
                    );

            Long userId =
                    user.getId();

            // =================================================
            // EXTRACT
            // =================================================

            String content =
                    extractionService
                            .extractText(file);

            if (
                    content == null ||
                    content.isBlank()
            ) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "The uploaded document contains no readable text"
                                )
                        );
            }

            // =================================================
            // INDEX
            // =================================================

            int indexedChunks =
                    indexDocument(
                            content,
                            file.getOriginalFilename(),
                            userId
                    );

            return ResponseEntity.ok(
                    Map.of(

                            "filename",
                            file.getOriginalFilename(),

                            "userId",
                            userId,

                            "indexedChunks",
                            indexedChunks
                    )
            );

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );

        } catch (IOException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "error",
                                    "Failed to process document"
                            )
                    );

        } catch (RuntimeException e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Embedding failed"
                            )
                    );
        }
    }
}