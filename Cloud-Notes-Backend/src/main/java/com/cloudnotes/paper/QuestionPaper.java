package com.cloudnotes.paper;

import com.cloudnotes.note.NoteFileItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "question_papers")
@CompoundIndexes({
        @CompoundIndex(name = "paper_branch_sem_idx", def = "{'branch': 1, 'semester': 1}"),
        @CompoundIndex(name = "paper_year_type_idx", def = "{'year': 1, 'examType': 1}")
})
public class QuestionPaper {

    @Id
    private String id;

    private String title;

    private String subject;

    private String branch;

    private Integer semester;

    private Integer year;

    private ExamType examType;

    private String description;

    @Indexed
    private String uploaderId;

    private String uploaderName;

    @Builder.Default
    private List<NoteFileItem> files = new ArrayList<>();

    @Builder.Default
    private long totalSize = 0L;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
