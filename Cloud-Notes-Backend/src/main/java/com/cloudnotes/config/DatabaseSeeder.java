package com.cloudnotes.config;

import com.cloudnotes.note.Note;
import com.cloudnotes.note.NoteFileItem;
import com.cloudnotes.note.NoteRepository;
import com.cloudnotes.paper.ExamType;
import com.cloudnotes.paper.QuestionPaper;
import com.cloudnotes.paper.QuestionPaperRepository;
import com.cloudnotes.user.Role;
import com.cloudnotes.user.User;
import com.cloudnotes.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final QuestionPaperRepository paperRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@cloudnotes.edu}")
    private String adminEmail;

    @Value("${app.seed.admin-password:Admin@CloudNotes2026}")
    private String adminPassword;

    @Value("${app.seed.admin-name:System Administrator}")
    private String adminName;

    @Value("${app.storage.local-dir:./uploads}")
    private String uploadDir;

    @Override
    public void run(String... args) {
        User admin = seedAdminUser();
        seedDemoDataFromFolder(admin);
    }

    private User seedAdminUser() {
        return userRepository.findByEmail(adminEmail.toLowerCase().trim())
                .orElseGet(() -> {
                    User newAdmin = User.builder()
                            .name(adminName)
                            .email(adminEmail.toLowerCase().trim())
                            .password(passwordEncoder.encode(adminPassword))
                            .branch("Computer Science")
                            .semester(4)
                            .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_STUDENT))
                            .enabled(true)
                            .emailVerified(true)
                            .build();

                    newAdmin = userRepository.save(newAdmin);
                    log.info("Default administrator seeded: {}", adminEmail);
                    return newAdmin;
                });
    }

    private void seedDemoDataFromFolder(User defaultUploader) {
        // Only seed demo data if notes collection is currently empty
        if (noteRepository.count() > 0) {
            log.info("Notes already present in DB ({} records). Skipping demo folder re-seeding.", noteRepository.count());
            return;
        }

        // Check for "Notes Data" in root folder
        Path demoRoot = Paths.get("..", "Notes Data").toAbsolutePath().normalize();
        if (!Files.exists(demoRoot)) {
            demoRoot = Paths.get("Notes Data").toAbsolutePath().normalize();
        }

        if (!Files.exists(demoRoot)) {
            log.warn("Demo folder 'Notes Data' not found at: {}", demoRoot);
            return;
        }

        log.info("Found Demo folder 'Notes Data' at: {}. Starting ingestion into MongoDB...", demoRoot);

        Path uploadsPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadsPath);
        } catch (IOException e) {
            log.error("Failed to ensure uploads directory: {}", uploadsPath, e);
            return;
        }

        File[] semDirs = demoRoot.toFile().listFiles(File::isDirectory);
        if (semDirs == null) return;

        int totalNotesSeeded = 0;
        int totalPapersSeeded = 0;

        for (File semDir : semDirs) {
            String semName = semDir.getName().toLowerCase();
            int semester = 4;
            if (semName.contains("5")) semester = 5;
            else if (semName.contains("1")) semester = 1;
            else if (semName.contains("2")) semester = 2;
            else if (semName.contains("3")) semester = 3;
            else if (semName.contains("6")) semester = 6;
            else if (semName.contains("7")) semester = 7;
            else if (semName.contains("8")) semester = 8;

            File[] subjectDirs = semDir.listFiles(File::isDirectory);
            if (subjectDirs == null) continue;

            for (File subDir : subjectDirs) {
                String subjectName = subDir.getName();

                List<File> allFiles = new ArrayList<>();
                collectFilesRecursively(subDir, allFiles);

                if (allFiles.isEmpty()) continue;

                // Separate question paper files from study notes files
                List<File> noteFiles = new ArrayList<>();
                List<File> paperFiles = new ArrayList<>();

                for (File f : allFiles) {
                    String lower = f.getName().toLowerCase();
                    // Exclude huge video files > 25MB or binaries to keep local database lean
                    if (f.length() > 30 * 1024 * 1024 || lower.endsWith(".mp4") || lower.endsWith(".jff")) {
                        continue;
                    }
                    if (lower.contains("qp") || lower.contains("question") || lower.contains("scheme") || lower.contains("test")) {
                        paperFiles.add(f);
                    } else {
                        noteFiles.add(f);
                    }
                }

                // Batch note files in chunks of up to 8 files per note
                if (!noteFiles.isEmpty()) {
                    List<List<File>> chunks = chunkList(noteFiles, 8);
                    int part = 1;
                    for (List<File> chunk : chunks) {
                        String title = subjectName + " Study Notes" + (chunks.size() > 1 ? " - Part " + part : "");
                        Note note = createNoteRecord(defaultUploader, title, subjectName, "Computer Science", semester, chunk, uploadsPath);
                        if (note != null) {
                            noteRepository.save(note);
                            totalNotesSeeded++;
                        }
                        part++;
                    }
                }

                // Batch paper files in chunks of up to 6 files per paper
                if (!paperFiles.isEmpty()) {
                    List<List<File>> chunks = chunkList(paperFiles, 6);
                    int part = 1;
                    for (List<File> chunk : chunks) {
                        String title = subjectName + " Previous Exam & Test Papers" + (chunks.size() > 1 ? " - Part " + part : "");
                        QuestionPaper paper = createPaperRecord(defaultUploader, title, subjectName, "Computer Science", semester, chunk, uploadsPath);
                        if (paper != null) {
                            paperRepository.save(paper);
                            totalPapersSeeded++;
                        }
                        part++;
                    }
                }
            }
        }

        log.info("Completed demo data seeding! Ingested {} Note collections and {} Question Paper archives.", totalNotesSeeded, totalPapersSeeded);
    }

    private void collectFilesRecursively(File dir, List<File> accumulator) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                // Skip node_modules or large build folders
                if (!f.getName().equalsIgnoreCase("node_modules")) {
                    collectFilesRecursively(f, accumulator);
                }
            } else {
                accumulator.add(f);
            }
        }
    }

    private Note createNoteRecord(User uploader, String title, String subject, String branch, int semester, List<File> files, Path uploadsPath) {
        List<NoteFileItem> items = copyFilesToUploads(files, uploadsPath);
        if (items.isEmpty()) return null;

        long totalSize = items.stream().mapToLong(NoteFileItem::getSize).sum();

        return Note.builder()
                .title(title)
                .subject(subject)
                .branch(branch)
                .semester(semester)
                .description("Course learning materials and lecture summaries for " + subject + " (Semester " + semester + ").")
                .uploaderId(uploader.getId())
                .uploaderName(uploader.getName())
                .files(items)
                .totalSize(totalSize)
                .downloadCount(0)
                .bookmarkCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private QuestionPaper createPaperRecord(User uploader, String title, String subject, String branch, int semester, List<File> files, Path uploadsPath) {
        List<NoteFileItem> items = copyFilesToUploads(files, uploadsPath);
        if (items.isEmpty()) return null;

        long totalSize = items.stream().mapToLong(NoteFileItem::getSize).sum();

        return QuestionPaper.builder()
                .title(title)
                .subject(subject)
                .branch(branch)
                .semester(semester)
                .year(2024)
                .examType(ExamType.END_SEMESTER)
                .description("Official question paper archives and evaluation schemes for " + subject + ".")
                .uploaderId(uploader.getId())
                .uploaderName(uploader.getName())
                .files(items)
                .totalSize(totalSize)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private List<NoteFileItem> copyFilesToUploads(List<File> files, Path uploadsPath) {
        List<NoteFileItem> result = new ArrayList<>();
        for (File f : files) {
            try {
                String safeName = f.getName();
                String ext = "";
                int dotIdx = safeName.lastIndexOf('.');
                if (dotIdx >= 0) ext = safeName.substring(dotIdx);

                String fileId = UUID.randomUUID().toString();
                String storedName = fileId + ext;
                Path target = uploadsPath.resolve(storedName);

                Files.copy(f.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

                String contentType = Files.probeContentType(f.toPath());
                if (contentType == null) contentType = "application/octet-stream";

                result.add(NoteFileItem.builder()
                        .fileId(fileId)
                        .originalFileName(safeName)
                        .storedFileName(storedName)
                        .contentType(contentType)
                        .size(f.length())
                        .downloadUrl("/api/files/" + storedName)
                        .build());
            } catch (IOException e) {
                log.warn("Could not copy file {} during seeding: {}", f.getName(), e.getMessage());
            }
        }
        return result;
    }

    private <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(list.size(), i + chunkSize)));
        }
        return chunks;
    }
}
