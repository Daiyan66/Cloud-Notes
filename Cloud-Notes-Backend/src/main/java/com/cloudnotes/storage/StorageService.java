package com.cloudnotes.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class StoredFileInfo {
        private String fileId;
        private String originalFileName;
        private String storedFileName;
        private String contentType;
        private long size;
        private String storagePath;
    }

    StoredFileInfo store(MultipartFile file) throws IOException;

    Resource loadAsResource(String storagePath) throws IOException;

    void delete(String storagePath);
}
