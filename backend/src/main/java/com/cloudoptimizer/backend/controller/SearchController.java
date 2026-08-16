package com.cloudoptimizer.backend.controller;

import com.cloudoptimizer.backend.dto.SearchRequest;
import com.cloudoptimizer.backend.service.RagService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SearchController {

    private final RagService ragService;

    public SearchController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestBody SearchRequest request
    ) {

        if (request == null ||
                request.getQuery() == null ||
                request.getQuery().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error",
                            "Query cannot be empty"
                    ));
        }

        Map<String, Object> response =
                ragService.generateAnswer(
                        request.getQuery().trim()
                );

        return ResponseEntity.ok(response);
    }
}