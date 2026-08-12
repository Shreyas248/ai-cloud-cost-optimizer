package com.cloudoptimizer.backend.model;

public class DocumentChunk {

    private int chunkIndex;

    private String content;

    public DocumentChunk(
            int chunkIndex,
            String content
    ) {
        this.chunkIndex = chunkIndex;
        this.content = content;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }
}