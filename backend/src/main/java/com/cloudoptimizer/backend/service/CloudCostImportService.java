package com.cloudoptimizer.backend.service;

import com.cloudoptimizer.backend.model.CloudCost;
import com.cloudoptimizer.backend.model.User;
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


    // =========================================================
    // IMPORT CSV
    // =========================================================

    public int importCsv(
            MultipartFile file,
            User user
    ) throws IOException {

        if (file == null ||
                file.isEmpty()) {

            throw new IllegalArgumentException(
                    "CSV file is empty"
            );
        }

        if (user == null) {

            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

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

            while (
                    (line = reader.readLine())
                            != null
            ) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }


                // =============================================
                // SKIP HEADER
                // =============================================

                if (header) {

                    header = false;

                    continue;
                }


                // =============================================
                // SPLIT CSV
                // =============================================

                String[] columns =
                        line.split(",");

                if (columns.length != 3) {

                    throw new IllegalArgumentException(
                            "Invalid CSV format. Expected: service,month,cost"
                    );
                }


                // =============================================
                // SERVICE
                // =============================================

                String service =
                        columns[0].trim();

                if (service.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Service cannot be empty"
                    );
                }


                // =============================================
                // MONTH
                // =============================================

                String month =
                        columns[1].trim();

                if (month.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Month cannot be empty"
                    );
                }


                // =============================================
                // COST
                // =============================================

                double cost;

                try {

                    cost =
                            Double.parseDouble(
                                    columns[2].trim()
                            );

                } catch (NumberFormatException e) {

                    throw new IllegalArgumentException(
                            "Invalid cost value: "
                                    + columns[2].trim()
                    );
                }


                if (cost < 0) {

                    throw new IllegalArgumentException(
                            "Cost cannot be negative"
                    );
                }


                // =============================================
                // CREATE CLOUD COST
                // =============================================

                CloudCost cloudCost =
                        new CloudCost(
                                user,
                                service,
                                month,
                                cost
                        );


                // =============================================
                // SAVE
                // =============================================

                repository.save(cloudCost);

                imported++;
            }
        }

        return imported;
    }
}