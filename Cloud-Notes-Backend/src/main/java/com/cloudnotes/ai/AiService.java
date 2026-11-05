package com.cloudnotes.ai;

import com.cloudnotes.note.Note;
import com.cloudnotes.note.NoteFileItem;
import com.cloudnotes.note.NoteRepository;
import com.cloudnotes.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiSummarizerProvider aiProvider;
    private final NoteRepository noteRepository;
    private final StorageService storageService;

    // In-memory cache for generated summaries by noteId + fileId
    private final Map<String, AiSummarizerProvider.SummaryResult> summaryCache = new ConcurrentHashMap<>();

    public AiSummarizerProvider.SummaryResult generateSummary(String noteId, String fileId) {
        String cacheKey = noteId + (fileId != null ? "_" + fileId : "");
        if (summaryCache.containsKey(cacheKey)) {
            log.info("Serving AI summary from cache for {}", cacheKey);
            return summaryCache.get(cacheKey);
        }

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + noteId));

        NoteFileItem targetFile = null;
        if (fileId != null) {
            targetFile = note.getFiles().stream()
                    .filter(f -> f.getFileId().equals(fileId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("File not found in this note."));
        } else {
            // Pick first PDF or document
            targetFile = note.getFiles().stream()
                    .filter(f -> f.getOriginalFileName().toLowerCase().endsWith(".pdf"))
                    .findFirst()
                    .orElse(note.getFiles().isEmpty() ? null : note.getFiles().get(0));
        }

        if (targetFile == null) {
            throw new IllegalArgumentException("No suitable file found to summarize.");
        }

        String extractedText;
        if (targetFile.getOriginalFileName().toLowerCase().endsWith(".pdf")) {
            extractedText = extractPdfText(targetFile.getStoredFileName());
        } else {
            extractedText = "Document Name: " + targetFile.getOriginalFileName() +
                    "\nSubject: " + note.getSubject() +
                    "\nTitle: " + note.getTitle() +
                    "\nDescription: " + (note.getDescription() != null ? note.getDescription() : "Standard student course material.");
        }

        if (extractedText.trim().length() < 15) {
            throw new IllegalArgumentException("Document does not contain sufficient extractable text (it might be a scanned image or empty).");
        }

        AiSummarizerProvider.SummaryResult result = aiProvider.summarizeDocument(targetFile.getOriginalFileName(), extractedText);
        summaryCache.put(cacheKey, result);
        return result;
    }

    private String extractPdfText(String storedFileName) {
        try {
            Resource resource = storageService.loadAsResource(storedFileName);
            File file = resource.getFile();
            try (PDDocument document = Loader.loadPDF(file)) {
                if (document.isEncrypted()) {
                    throw new IllegalArgumentException("PDF is password protected and cannot be extracted.");
                }
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                // Limit to first 10 pages for cost and size protection
                stripper.setStartPage(1);
                stripper.setEndPage(Math.min(10, document.getNumberOfPages()));
                return stripper.getText(document);
            }
        } catch (IOException e) {
            log.error("Failed to extract PDF text from {}", storedFileName, e);
            throw new IllegalArgumentException("Could not extract text from the PDF file: " + e.getMessage());
        }
    }
}
