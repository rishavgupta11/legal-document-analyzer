package com.legaldocanalyzer.service.impl;

import com.legaldocanalyzer.config.FileStorageConfig;
import com.legaldocanalyzer.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageServiceImpl(FileStorageConfig fileStorageConfig) {
        // Get the upload directory path
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath()
                .normalize();

        try {
            // Create the directory if it doesn't exist
            Files.createDirectories(this.fileStorageLocation);
            logger.info("File storage directory created at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("Could not create upload directory", ex);
            throw new RuntimeException("Could not create upload directory: " + ex.getMessage());
        }
    }

    @Override
    public String storeFile(MultipartFile file, String userId) throws Exception {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new Exception("Failed to store empty file");
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.contains("..")) {
                throw new Exception("Invalid file name: " + originalFileName);
            }

            // Generate unique filename
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = generateUniqueFileName(userId) + "." + fileExtension;

            // Create user-specific directory
            Path userDirectory = this.fileStorageLocation.resolve(userId);
            Files.createDirectories(userDirectory);  // ← IMPORTANT: Create user folder

            // Copy file to the target location
            Path targetLocation = userDirectory.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File stored successfully at: {}", targetLocation);

            // Return ABSOLUTE path for storage in DB
            return targetLocation.toString();  // ← Changed: return absolute path

        } catch (IOException ex) {
            logger.error("Failed to store file", ex);
            throw new Exception("Failed to store file: " + ex.getMessage());
        }
    }

    @Override
    public String getFileContent(String filePath) throws Exception {
        try {
            // Handle both absolute and relative paths
            Path file = Paths.get(filePath);

            // If relative path, resolve against storage location
            if (!file.isAbsolute()) {
                file = this.fileStorageLocation.resolve(filePath).normalize();
            }

            if (!Files.exists(file)) {
                throw new Exception("File not found: " + filePath);
            }

            return file.toString();

        } catch (Exception ex) {
            logger.error("Failed to get file: {}", filePath, ex);
            throw new Exception("Failed to get file: " + ex.getMessage());
        }
    }

    @Override
    public void deleteFile(String filePath) throws Exception {
        try {
            Path fileToDelete = Paths.get(filePath);

            // If relative path, resolve it
            if (!fileToDelete.isAbsolute()) {
                fileToDelete = this.fileStorageLocation.resolve(filePath).normalize();
            }

            boolean deleted = Files.deleteIfExists(fileToDelete);

            if (deleted) {
                logger.info("File deleted successfully: {}", filePath);
            } else {
                logger.warn("File not found for deletion: {}", filePath);
            }

        } catch (IOException ex) {
            logger.error("Failed to delete file: {}", filePath, ex);
            throw new Exception("Failed to delete file: " + ex.getMessage());
        }
    }

    private String generateUniqueFileName(String userId) {
        return System.currentTimeMillis() + "_" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}