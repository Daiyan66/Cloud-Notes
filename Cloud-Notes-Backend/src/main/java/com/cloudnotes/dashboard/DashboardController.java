package com.cloudnotes.dashboard;

import com.cloudnotes.common.ApiResponse;
import com.cloudnotes.note.Note;
import com.cloudnotes.note.NoteRepository;
import com.cloudnotes.paper.QuestionPaper;
import com.cloudnotes.paper.QuestionPaperRepository;
import com.cloudnotes.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final NoteRepository noteRepository;
    private final QuestionPaperRepository paperRepository;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStats {
        private long totalNotesUploaded;
        private long totalPapersUploaded;
        private long aggregateFileSize;
        private int totalBookmarks;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStats>> getStats(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User not authenticated.");
        }

        List<Note> userNotes = noteRepository.findByUploaderId(currentUser.getId());
        List<QuestionPaper> userPapers = paperRepository.findByUploaderId(currentUser.getId());

        long noteBytes = userNotes.stream().mapToLong(Note::getTotalSize).sum();
        long paperBytes = userPapers.stream().mapToLong(QuestionPaper::getTotalSize).sum();

        DashboardStats stats = DashboardStats.builder()
                .totalNotesUploaded(userNotes.size())
                .totalPapersUploaded(userPapers.size())
                .aggregateFileSize(noteBytes + paperBytes)
                .totalBookmarks(0)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
