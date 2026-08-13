package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkerService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    public List<DocumentChunk> chunkText(String text) {

        List<DocumentChunk> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        // Normalize line endings and whitespace
        text = text.replace("\r\n", "\n")
                   .replace("\r", "\n")
                   .trim();

        // Split into paragraphs
        String[] paragraphs = text.split("\\n\\s*\\n");

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {

            paragraph = paragraph.trim();

            if (paragraph.isEmpty()) {
                continue;
            }

            // If adding the paragraph keeps us within the target size
            if (currentChunk.length() + paragraph.length() + 2 <= CHUNK_SIZE) {

                if (!currentChunk.isEmpty()) {
                    currentChunk.append("\n\n");
                }

                currentChunk.append(paragraph);

            } else {

                // Save current chunk
                if (!currentChunk.isEmpty()) {

                    chunks.add(
                            new DocumentChunk(
                                    chunks.size(),
                                    currentChunk.toString().trim()
                            )
                    );
                }

                // Handle paragraphs larger than CHUNK_SIZE
                if (paragraph.length() > CHUNK_SIZE) {

                    List<String> smallerParts =
                            splitLargeParagraph(paragraph);

                    for (String part : smallerParts) {

                        chunks.add(
                                new DocumentChunk(
                                        chunks.size(),
                                        part
                            )
                        );
                    }

                    currentChunk.setLength(0);

                } else {

                    currentChunk.setLength(0);
                    currentChunk.append(paragraph);
                }
            }
        }

        // Add remaining text
        if (!currentChunk.isEmpty()) {

            chunks.add(
                    new DocumentChunk(
                            chunks.size(),
                            currentChunk.toString().trim()
                    )
            );
        }

        return chunks;
    }

    private List<String> splitLargeParagraph(String paragraph) {

        List<String> parts = new ArrayList<>();

        // First try to split by sentences
        String[] sentences =
                paragraph.split("(?<=[.!?])\\s+");

        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {

            sentence = sentence.trim();

            if (sentence.isEmpty()) {
                continue;
            }

            if (current.length() + sentence.length() + 1 <= CHUNK_SIZE) {

                if (!current.isEmpty()) {
                    current.append(" ");
                }

                current.append(sentence);

            } else {

                if (!current.isEmpty()) {

                    parts.add(current.toString().trim());
                }

                current.setLength(0);
                current.append(sentence);
            }
        }

        if (!current.isEmpty()) {
            parts.add(current.toString().trim());
        }

        // Safety fallback for extremely long sentences
        List<String> finalParts = new ArrayList<>();

        for (String part : parts) {

            if (part.length() <= CHUNK_SIZE) {

                finalParts.add(part);

            } else {

                finalParts.addAll(
                        splitByWords(part)
                );
            }
        }

        return finalParts;
    }

    private List<String> splitByWords(String text) {

        List<String> parts = new ArrayList<>();

        String[] words = text.split("\\s+");

        StringBuilder current = new StringBuilder();

        for (String word : words) {

            if (current.length() + word.length() + 1 <= CHUNK_SIZE) {

                if (!current.isEmpty()) {
                    current.append(" ");
                }

                current.append(word);

            } else {

                if (!current.isEmpty()) {

                    parts.add(
                            current.toString().trim()
                    );
                }

                current.setLength(0);
                current.append(word);
            }
        }

        if (!current.isEmpty()) {

            parts.add(
                    current.toString().trim()
            );
        }

        return parts;
    }
}