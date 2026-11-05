package com.cloudnotes.paper;

import com.cloudnotes.common.PageResponse;
import com.cloudnotes.note.NoteFileItem;
import com.cloudnotes.storage.StorageService;
import com.cloudnotes.user.Role;
import com.cloudnotes.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionPaperService {

    private final QuestionPaperRepository paperRepository;
    private final StorageService storageService;
    private final MongoTemplate mongoTemplate;

    private static final int MAX_FILES_PER_UPLOAD = 10;
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    public PaperDto.Response createPaper(User uploader, PaperDto.CreateRequest request, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one question paper file is required.");
        }

        if (files.size() > MAX_FILES_PER_UPLOAD) {
            throw new IllegalArgumentException("You can upload a maximum of " + MAX_FILES_PER_UPLOAD + " files.");
        }

        List<NoteFileItem> fileItems = new ArrayList<>();
        long totalSize = 0L;

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new IllegalArgumentException("File '" + file.getOriginalFilename() + "' exceeds 10MB limit.");
            }

            try {
                StorageService.StoredFileInfo info = storageService.store(file);
                fileItems.add(NoteFileItem.builder()
                        .fileId(info.getFileId())
                        .originalFileName(info.getOriginalFileName())
                        .storedFileName(info.getStoredFileName())
                        .contentType(info.getContentType())
                        .size(info.getSize())
                        .downloadUrl("/api/files/" + info.getStoredFileName())
                        .build());
                totalSize += info.getSize();
            } catch (IOException e) {
                for (NoteFileItem item : fileItems) {
                    storageService.delete(item.getStoredFileName());
                }
                throw new RuntimeException("Could not upload file: " + file.getOriginalFilename(), e);
            }
        }

        QuestionPaper paper = QuestionPaper.builder()
                .title(request.getTitle().trim())
                .subject(request.getSubject().trim())
                .branch(request.getBranch() != null ? request.getBranch().trim() : "General")
                .semester(request.getSemester() != null ? request.getSemester() : 1)
                .year(request.getYear() != null ? request.getYear() : 2024)
                .examType(request.getExamType() != null ? request.getExamType() : ExamType.END_SEMESTER)
                .description(request.getDescription())
                .uploaderId(uploader.getId())
                .uploaderName(uploader.getName())
                .files(fileItems)
                .totalSize(totalSize)
                .build();

        paper = paperRepository.save(paper);
        return mapToDto(paper);
    }

    public PageResponse<PaperDto.Response> searchPapers(
            String queryText, String branch, Integer semester, Integer year, ExamType examType, String subject, int page, int size) {

        int boundedSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(page, boundedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Query query = new Query().with(pageable);
        List<Criteria> criteriaList = new ArrayList<>();

        if (StringUtils.hasText(queryText)) {
            String regex = ".*" + queryText.trim() + ".*";
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(regex, "i"),
                    Criteria.where("subject").regex(regex, "i"),
                    Criteria.where("description").regex(regex, "i")
            ));
        }

        if (StringUtils.hasText(branch) && !branch.equalsIgnoreCase("all")) {
            criteriaList.add(Criteria.where("branch").is(branch));
        }

        if (semester != null && semester > 0) {
            criteriaList.add(Criteria.where("semester").is(semester));
        }

        if (year != null && year > 1900) {
            criteriaList.add(Criteria.where("year").is(year));
        }

        if (examType != null) {
            criteriaList.add(Criteria.where("examType").is(examType));
        }

        if (StringUtils.hasText(subject) && !subject.equalsIgnoreCase("all")) {
            criteriaList.add(Criteria.where("subject").regex("^" + subject.trim() + "$", "i"));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<QuestionPaper> papers = mongoTemplate.find(query, QuestionPaper.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), QuestionPaper.class);

        Page<QuestionPaper> paperPage = PageableExecutionUtils.getPage(papers, pageable, () -> count);
        return PageResponse.from(paperPage.map(this::mapToDto));
    }

    public PaperDto.Response getPaperById(String id) {
        QuestionPaper paper = paperRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found with id: " + id));
        return mapToDto(paper);
    }

    public PageResponse<PaperDto.Response> getPapersByUploader(String uploaderId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<QuestionPaper> paperPage = paperRepository.findByUploaderId(uploaderId, pageable);
        return PageResponse.from(paperPage.map(this::mapToDto));
    }

    public void deletePaper(String paperId, User currentUser) {
        QuestionPaper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Paper not found with id: " + paperId));

        boolean isAdmin = currentUser.getRoles().contains(Role.ROLE_ADMIN);
        boolean isOwner = paper.getUploaderId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to delete this paper.");
        }

        if (paper.getFiles() != null) {
            for (NoteFileItem item : paper.getFiles()) {
                storageService.delete(item.getStoredFileName());
            }
        }

        paperRepository.delete(paper);
    }

    public PaperDto.Response mapToDto(QuestionPaper paper) {
        return PaperDto.Response.builder()
                .id(paper.getId())
                .title(paper.getTitle())
                .subject(paper.getSubject())
                .branch(paper.getBranch())
                .semester(paper.getSemester())
                .year(paper.getYear())
                .examType(paper.getExamType())
                .description(paper.getDescription())
                .uploaderId(paper.getUploaderId())
                .uploaderName(paper.getUploaderName())
                .files(paper.getFiles())
                .totalSize(paper.getTotalSize())
                .createdAt(paper.getCreatedAt())
                .updatedAt(paper.getUpdatedAt())
                .build();
    }
}
