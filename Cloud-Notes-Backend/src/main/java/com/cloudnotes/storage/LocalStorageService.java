package com.cloudnotes.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;
    private final Tika tika = new Tika();

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
            "application/vnd.ms-powerpoint", // .ppt
            "text/plain", // .txt
            "application/zip", // .zip
            "application/x-zip-compressed"
    );

    public LocalStorageService(@Value("${app.storage.local-dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public StoredFileInfo store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file.");
        }

        // Validate content-type with Apache Tika inspection (magic bytes)
        String detectedType = tika.detect(file.getInputStream());
        log.info("Detected mime-type for {}: {}", file.getOriginalFilename(), detectedType);

        if (!ALLOWED_MIME_TYPES.contains(detectedType)) {
            // Also check declared type if tika is generic octet-stream
            if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
                throw new IllegalArgumentException("Unsupported file type (" + detectedType + "). Only PDF, JPG, PNG, DOC, and DOCX are allowed.");
            }
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        // Sanitize filename
        String safeName = Paths.get(originalFilename).getFileName().toString();
        String fileExtension = "";
        int dotIndex = safeName.lastIndexOf('.');
        if (dotIndex >= 0) {
            fileExtension = safeName.substring(dotIndex);
        }

        String fileId = UUID.randomUUID().toString();
        String storedFileName = fileId + fileExtension;
        Path targetPath = this.rootLocation.resolve(storedFileName).normalize();

        if (!targetPath.getParent().equals(this.rootLocation)) {
            throw new SecurityException("Cannot store file outside current directory.");
        }

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return StoredFileInfo.builder()
                .fileId(fileId)
                .originalFileName(safeName)
                .storedFileName(storedFileName)
                .contentType(detectedType)
                .size(file.getSize())
                .storagePath(storedFileName)
                .build();
    }

    @Override
    public Resource loadAsResource(String storagePath) throws IOException {
        try {
            Path file = rootLocation.resolve(storagePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + storagePath);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("Could not read file: " + storagePath);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path file = rootLocation.resolve(storagePath).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Failed to delete file from storage: {}", storagePath, e);
        }
    }
}
