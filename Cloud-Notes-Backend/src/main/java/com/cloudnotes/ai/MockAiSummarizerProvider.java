package com.cloudnotes.ai;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MockAiSummarizerProvider implements AiSummarizerProvider {

    @Override
    public SummaryResult summarizeDocument(String documentTitle, String extractedText) {
        String cleanSnippet = extractedText.replaceAll("\\s+", " ").trim();
        if (cleanSnippet.length() > 300) {
            cleanSnippet = cleanSnippet.substring(0, 300) + "...";
        }

        List<String> lines = Arrays.stream(extractedText.split("\n"))
                .map(String::trim)
                .filter(l -> l.length() > 20)
                .limit(5)
                .collect(Collectors.toList());

        StringBuilder md = new StringBuilder();
        md.append("### \uD83D\uDCDA AI Study Summary: ").append(documentTitle).append("\n\n");
        md.append("> **Disclaimer**: AI output may be incomplete or imprecise. Please review against the uploaded study material.\n\n");
        md.append("#### \uD83D\uDCA1 Key Concepts & Objectives\n");
        md.append("- Comprehensive overview of core exam topics identified in **").append(documentTitle).append("**.\n");
        md.append("- High-yield definitions, structured principles, and algorithmic patterns.\n\n");
        md.append("#### \uD83D\uDD11 Important Points\n");
        if (lines.isEmpty()) {
            md.append("- Document context extracted: \"").append(cleanSnippet).append("\"\n");
        } else {
            for (String line : lines) {
                md.append("- ").append(line).append("\n");
            }
        }
        md.append("\n#### ⚡ Quick Takeaways\n");
        md.append("1. Pay close attention to standard formulas and diagrammatic representations.\n");
        md.append("2. Solve previous question paper patterns related to these chapters.\n");
        md.append("3. Review definitions and revision summaries before testing yourself.\n");

        return SummaryResult.builder()
                .title("Summary for " + documentTitle)
                .markdownContent(md.toString())
                .provider("CloudNotes Smart Summarizer (Built-in Engine)")
                .estimatedReadTimeMinutes(Math.max(1, extractedText.length() / 1500))
                .build();
    }
}
