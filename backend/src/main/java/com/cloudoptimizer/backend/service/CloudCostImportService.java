package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.model.CloudCost;
import com.cloudoptimizer.backend.repository.CloudCostRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class CloudCostImportService {

    private final CloudCostRepository repository;

    public CloudCostImportService(
            CloudCostRepository repository
    ) {
        this.repository = repository;
    }

    public int importCsv(
            MultipartFile file
    ) throws IOException {

        int imported = 0;

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        file.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            boolean header = true;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                // Skip CSV header
                if (header) {
                    header = false;
                    continue;
                }

                String[] columns =
                        line.split(",");

                if (columns.length != 3) {
                    continue;
                }

                String service =
                        columns[0].trim();

                String month =
                        columns[1].trim();

                double cost =
                        Double.parseDouble(
                                columns[2].trim()
                        );

                CloudCost cloudCost =
                        new CloudCost(
                                service,
                                month,
                                cost
                        );

                repository.save(cloudCost);

                imported++;
            }
        }

        return imported;
    }
}