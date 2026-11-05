package com.cloudnotes.paper;

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
@RequestMapping("/papers")
@RequiredArgsConstructor
public class QuestionPaperController {

    private final QuestionPaperService paperService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PaperDto.Response>> createPaper(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("title") String title,
            @RequestParam("subject") String subject,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "semester", required = false) Integer semester,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "examType", required = false) ExamType examType,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart("files") List<MultipartFile> files) {

        if (currentUser == null) {
            throw new IllegalArgumentException("User must be authenticated to upload question papers.");
        }

        PaperDto.CreateRequest request = PaperDto.CreateRequest.builder()
                .title(title)
                .subject(subject)
                .branch(branch)
                .semester(semester)
                .year(year)
                .examType(examType)
                .description(description)
                .build();

        PaperDto.Response response = paperService.createPaper(currentUser, request, files);
        return ResponseEntity.ok(ApiResponse.ok("Question paper uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaperDto.Response>>> getPapers(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "semester", required = false) Integer semester,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "examType", required = false) ExamType examType,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        PageResponse<PaperDto.Response> result = paperService.searchPapers(query, branch, semester, year, examType, subject, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaperDto.Response>> getPaperById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(paperService.getPaperById(id)));
    }

    @GetMapping("/my-uploads")
    public ResponseEntity<ApiResponse<PageResponse<PaperDto.Response>>> getMyUploads(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User not authenticated.");
        }
        return ResponseEntity.ok(ApiResponse.ok(paperService.getPapersByUploader(currentUser.getId(), page, size)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePaper(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User not authenticated.");
        }
        paperService.deletePaper(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Question paper deleted successfully", null));
    }
}
