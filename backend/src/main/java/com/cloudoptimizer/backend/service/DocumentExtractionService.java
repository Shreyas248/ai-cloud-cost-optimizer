package com.cloudoptimizer.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class DocumentExtractionService {

    public String extractText(MultipartFile file) throws IOException {

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                    "File name is missing"
            );
        }

        String extension =
                filename.substring(
                        filename.lastIndexOf('.') + 1
                ).toLowerCase();

        switch (extension) {

            case "txt":
                return extractTxt(file);

            case "csv":
                return extractCsv(file);

            case "pdf":
                return extractPdf(file);

            default:
                throw new IllegalArgumentException(
                        "Unsupported file type: " + extension
                );
        }
    }

    private String extractTxt(
            MultipartFile file
    ) throws IOException {

        return new String(
                file.getBytes(),
                StandardCharsets.UTF_8
        );
    }

    private String extractCsv(
            MultipartFile file
    ) throws IOException {

        return new String(
                file.getBytes(),
                StandardCharsets.UTF_8
        );
    }

    private String extractPdf(
            MultipartFile file
    ) throws IOException {

        try (
                PDDocument document =
                        Loader.loadPDF(file.getBytes())
        ) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            return stripper.getText(document);
        }
    }
}