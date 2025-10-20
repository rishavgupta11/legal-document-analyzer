package com.legaldocanalyzer.service;

public interface TextExtractionService {
    String extractText(String filePath, String contentType) throws Exception;
}
