package com.cloudnotes.note;

import com.cloudnotes.common.PageResponse;
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
public class NoteService {

    private final NoteRepository noteRepository;
    private final StorageService storageService;
    private final MongoTemplate mongoTemplate;

    private static final int MAX_FILES_PER_UPLOAD = 10;
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    public NoteDto.Response createNote(User uploader, NoteDto.CreateRequest request, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one file is required for notes upload.");
        }

        if (files.size() > MAX_FILES_PER_UPLOAD) {
            throw new IllegalArgumentException("You can upload a maximum of " + MAX_FILES_PER_UPLOAD + " files per note.");
        }

        List<NoteFileItem> fileItems = new ArrayList<>();
        long totalSize = 0L;

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new IllegalArgumentException("File '" + file.getOriginalFilename() + "' exceeds 10MB size limit.");
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
                log.error("Failed to store file {}", file.getOriginalFilename(), e);
                // Clean up already uploaded files if partial fail
                for (NoteFileItem item : fileItems) {
                    storageService.delete(item.getStoredFileName());
                }
                throw new RuntimeException("Could not upload file: " + file.getOriginalFilename(), e);
            }
        }

        Note note = Note.builder()
                .title(request.getTitle().trim())
                .subject(request.getSubject().trim())
                .branch(request.getBranch() != null ? request.getBranch().trim() : "General")
                .semester(request.getSemester() != null ? request.getSemester() : 1)
                .description(request.getDescription())
                .uploaderId(uploader.getId())
                .uploaderName(uploader.getName())
                .files(fileItems)
                .totalSize(totalSize)
                .build();

        note = noteRepository.save(note);
        return mapToDto(note);
    }

    public PageResponse<NoteDto.Response> searchNotes(
            String queryText, String branch, Integer semester, String subject, int page, int size) {

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

        if (StringUtils.hasText(subject) && !subject.equalsIgnoreCase("all")) {
            criteriaList.add(Criteria.where("subject").regex("^" + subject.trim() + "$", "i"));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        List<Note> notes = mongoTemplate.find(query, Note.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Note.class);

        Page<Note> notePage = PageableExecutionUtils.getPage(notes, pageable, () -> count);
        return PageResponse.from(notePage.map(this::mapToDto));
    }

    public NoteDto.Response getNoteById(String id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with id: " + id));
        return mapToDto(note);
    }

    public PageResponse<NoteDto.Response> getNotesByUploader(String uploaderId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> notePage = noteRepository.findByUploaderId(uploaderId, pageable);
        return PageResponse.from(notePage.map(this::mapToDto));
    }

    public void deleteNote(String noteId, User currentUser) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with id: " + noteId));

        boolean isAdmin = currentUser.getRoles().contains(Role.ROLE_ADMIN);
        boolean isOwner = note.getUploaderId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not allowed to delete this note.");
        }

        // Delete physical files
        if (note.getFiles() != null) {
            for (NoteFileItem fileItem : note.getFiles()) {
                storageService.delete(fileItem.getStoredFileName());
            }
        }

        noteRepository.delete(note);
    }

    public NoteDto.Response mapToDto(Note note) {
        return NoteDto.Response.builder()
                .id(note.getId())
                .title(note.getTitle())
                .subject(note.getSubject())
                .branch(note.getBranch())
                .semester(note.getSemester())
                .description(note.getDescription())
                .uploaderId(note.getUploaderId())
                .uploaderName(note.getUploaderName())
                .files(note.getFiles())
                .totalSize(note.getTotalSize())
                .downloadCount(note.getDownloadCount())
                .bookmarkCount(note.getBookmarkCount())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
