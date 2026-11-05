package com.cloudnotes.admin;

import com.cloudnotes.auth.AuthDto;
import com.cloudnotes.auth.AuthService;
import com.cloudnotes.common.ApiResponse;
import com.cloudnotes.common.PageResponse;
import com.cloudnotes.note.Note;
import com.cloudnotes.note.NoteRepository;
import com.cloudnotes.paper.QuestionPaper;
import com.cloudnotes.paper.QuestionPaperRepository;
import com.cloudnotes.user.User;
import com.cloudnotes.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final QuestionPaperRepository paperRepository;
    private final AuthService authService;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminOverviewStats {
        private long totalUsers;
        private long totalNotes;
        private long totalPapers;
        private Map<Integer, Long> notesBySemester;
        private Map<String, Long> notesByBranch;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminOverviewStats>> getAdminStats() {
        long totalUsers = userRepository.count();
        long totalNotes = noteRepository.count();
        long totalPapers = paperRepository.count();

        Map<Integer, Long> notesBySemester = new HashMap<>();
        for (int sem = 1; sem <= 8; sem++) {
            notesBySemester.put(sem, noteRepository.countBySemester(sem));
        }

        List<String> branches = List.of("Computer Science", "Information Technology", "Electronics", "Mechanical", "Civil", "Electrical");
        Map<String, Long> notesByBranch = new HashMap<>();
        for (String b : branches) {
            notesByBranch.put(b, noteRepository.countByBranch(b));
        }

        AdminOverviewStats stats = AdminOverviewStats.builder()
                .totalUsers(totalUsers)
                .totalNotes(totalNotes)
                .totalPapers(totalPapers)
                .notesBySemester(notesBySemester)
                .notesByBranch(notesByBranch)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<AuthDto.UserDto>>> listUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(users.map(authService::mapToUserDto))));
    }

    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<String>> toggleUserStatus(@PathVariable String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("User status updated. Enabled: " + user.isEnabled()));
    }
}
