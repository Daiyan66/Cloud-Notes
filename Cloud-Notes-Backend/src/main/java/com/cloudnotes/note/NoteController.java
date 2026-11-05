package com.cloudnotes.note;

import com.cloudnotes.common.ApiResponse;
import com.cloudnotes.common.PageResponse;
import com.cloudnotes.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<NoteDto.Response>> createNote(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("title") String title,
            @RequestParam("subject") String subject,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "semester", required = false) Integer semester,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart("files") List<MultipartFile> files) {

        if (currentUser == null) {
            throw new IllegalArgumentException("User must be authenticated to upload notes.");
        }

        NoteDto.CreateRequest request = NoteDto.CreateRequest.builder()
                .title(title)
                .subject(subject)
                .branch(branch)
                .semester(semester)
                .description(description)
                .build();

        NoteDto.Response response = noteService.createNote(currentUser, request, files);
        return ResponseEntity.ok(ApiResponse.ok("Note uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NoteDto.Response>>> getNotes(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "semester", required = false) Integer semester,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        PageResponse<NoteDto.Response> result = noteService.searchNotes(query, branch, semester, subject, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoteDto.Response>> getNoteById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(noteService.getNoteById(id)));
    }

    @GetMapping("/my-uploads")
    public ResponseEntity<ApiResponse<PageResponse<NoteDto.Response>>> getMyUploads(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User not authenticated.");
        }
        return ResponseEntity.ok(ApiResponse.ok(noteService.getNotesByUploader(currentUser.getId(), page, size)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User not authenticated.");
        }
        noteService.deleteNote(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Note deleted successfully", null));
    }
}
