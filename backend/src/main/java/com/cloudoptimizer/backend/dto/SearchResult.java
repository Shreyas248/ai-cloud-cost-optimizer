package com.cloudoptimizer.backend.dto;

public class SearchResult {

    private String content;
    private float score;
    private int chunkIndex;
    private String filename;

    public SearchResult() {
    }

    public SearchResult(
            String content,
            float score,
            int chunkIndex,
            String filename
    ) {
        this.content = content;
        this.score = score;
        this.chunkIndex = chunkIndex;
        this.filename = filename;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}