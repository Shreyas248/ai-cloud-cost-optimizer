package com.cloudoptimizer.backend.controller;
import java.util.List;
import com.cloudoptimizer.backend.service.DocumentExtractionService;
import com.cloudoptimizer.backend.model.DocumentChunk;
import com.cloudoptimizer.backend.service.TextChunkerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    private final Path uploadDirectory;

    private final DocumentExtractionService extractionService;
    private final TextChunkerService textChunkerService;

    public DocumentController(
        DocumentExtractionService extractionService,
        TextChunkerService textChunkerService
) {

    this.extractionService =
            extractionService;

    this.textChunkerService =
            textChunkerService;

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
}