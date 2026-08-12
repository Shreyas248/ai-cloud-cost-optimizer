package com.cloudoptimizer.backend.service;

import org.apache.pdfbox.Loader;
import com.cloudoptimizer.backend.service.DocumentExtractionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class DocumentExtractionService {

    public String extractText(
            MultipartFile file
    ) throws IOException {

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid filename"
            );
        }

        String extension = getExtension(filename);

        if ("pdf".equals(extension)) {
            return extractPdf(file);
        }

        if ("csv".equals(extension)) {
            return extractCsv(file);
        }

        throw new IllegalArgumentException(
                "Unsupported file type: " + extension
        );
    }

    private String extractPdf(
            MultipartFile file
    ) throws IOException {

        byte[] fileBytes = file.getBytes();

        try (PDDocument document =
                     Loader.loadPDF(fileBytes)) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            return stripper.getText(document);
        }
    }

    private String extractCsv(
            MultipartFile file
    ) throws IOException {

        byte[] fileBytes = file.getBytes();

        return new String(
                fileBytes,
                StandardCharsets.UTF_8
        );
    }

    private String getExtension(
            String filename
    ) {

        int lastDot =
                filename.lastIndexOf(".");

        if (lastDot == -1) {
            return "";
        }

        return filename
                .substring(lastDot + 1)
                .toLowerCase();
    }
}