package com.cloudnotes.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface AiSummarizerProvider {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SummaryResult {
        private String title;
        private String markdownContent;
        private String provider;
        private int estimatedReadTimeMinutes;
    }

    SummaryResult summarizeDocument(String documentTitle, String extractedText);
}
