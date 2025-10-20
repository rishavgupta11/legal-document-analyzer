package com.legaldocanalyzer.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String userId) throws Exception;
    void deleteFile(String filePath) throws Exception;
    String getFileContent(String filePath) throws Exception;
}