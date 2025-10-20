package com.legaldocanalyzer.service.impl;

import com.legaldocanalyzer.service.TextExtractionService;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class TextExtractionServiceImpl implements TextExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(TextExtractionServiceImpl.class);

    private final Tika tika;

    public TextExtractionServiceImpl() {
        this.tika = new Tika();
        this.tika.setMaxStringLength(10 * 1024 * 1024); // 10MB of text
    }

    @Override
    public String extractText(String filePath, String contentType) throws Exception {
        logger.info("Extracting text from file: {}", filePath);

        try {
            // Convert to absolute path if needed
            Path path = Paths.get(filePath);

            // Check if file exists
            if (!Files.exists(path)) {
                logger.error("File does not exist: {}", filePath);
                throw new Exception("File not found: " + filePath);
            }

            File file = path.toFile();

            if (!file.canRead()) {
                throw new Exception("Cannot read file: " + filePath);
            }

            // Extract text using Apache Tika
            String extractedText;

            try (FileInputStream inputStream = new FileInputStream(file)) {
                extractedText = tika.parseToString(inputStream);
            }

            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new Exception("No text could be extracted from the document");
            }

            // Clean and normalize the text
            extractedText = cleanExtractedText(extractedText);

            logger.info("Text extraction completed. Extracted {} characters", extractedText.length());

            return extractedText;

        } catch (IOException ex) {
            logger.error("IO error during text extraction from: {}", filePath, ex);
            throw new Exception("Failed to read file: " + ex.getMessage());

        } catch (TikaException ex) {
            logger.error("Tika error during text extraction from: {}", filePath, ex);
            throw new Exception("Failed to extract text from document: " + ex.getMessage());

        } catch (Exception ex) {
            logger.error("Unexpected error during text extraction from: {}", filePath, ex);
            throw new Exception("Text extraction failed: " + ex.getMessage());
        }
    }

    private String cleanExtractedText(String text) {
        if (text == null) {
            return "";
        }

        // Remove excessive whitespace
        text = text.replaceAll("\\s+", " ");

        // Remove control characters except newlines and tabs
        text = text.replaceAll("[\\x00-\\x09\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        // Normalize line breaks
        text = text.replaceAll("\\r\\n", "\n");
        text = text.replaceAll("\\r", "\n");

        // Remove multiple consecutive newlines
        text = text.replaceAll("\n{3,}", "\n\n");

        return text.trim();
    }
}