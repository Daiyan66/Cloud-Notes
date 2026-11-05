package com.cloudnotes.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

public class NoteDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String title;
        private String subject;
        private String branch;
        private Integer semester;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String id;
        private String title;
        private String subject;
        private String branch;
        private Integer semester;
        private String description;
        private String uploaderId;
        private String uploaderName;
        private List<NoteFileItem> files;
        private long totalSize;
        private int downloadCount;
        private int bookmarkCount;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
