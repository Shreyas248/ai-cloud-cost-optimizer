package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkerService {

    private static final int CHUNK_SIZE = 500;

    private static final int CHUNK_OVERLAP = 100;

    public List<DocumentChunk> chunkText(
            String text
    ) {

        List<DocumentChunk> chunks =
                new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        text = text.trim();

        int start = 0;

        int chunkIndex = 0;

        while (start < text.length()) {

            int end =
                    Math.min(
                            start + CHUNK_SIZE,
                            text.length()
                    );

            String chunk =
                    text.substring(start, end)
                            .trim();

            if (!chunk.isBlank()) {

                chunks.add(
                        new DocumentChunk(
                                chunkIndex,
                                chunk
                        )
                );

                chunkIndex++;
            }

            if (end >= text.length()) {
                break;
            }

            start =
                    end - CHUNK_OVERLAP;
        }

        return chunks;
    }
}