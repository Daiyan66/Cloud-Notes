package com.cloudnotes.ai;

import com.cloudnotes.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/summarize")
    public ResponseEntity<ApiResponse<AiSummarizerProvider.SummaryResult>> summarize(
            @RequestParam("noteId") String noteId,
            @RequestParam(value = "fileId", required = false) String fileId) {
        AiSummarizerProvider.SummaryResult result = aiService.generateSummary(noteId, fileId);
        return ResponseEntity.ok(ApiResponse.ok("Summary generated successfully", result));
    }
}
