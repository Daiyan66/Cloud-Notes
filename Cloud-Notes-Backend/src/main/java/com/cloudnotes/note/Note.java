package com.cloudnotes.note;

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
@Document(collection = "notes")
@CompoundIndexes({
        @CompoundIndex(name = "branch_semester_idx", def = "{'branch': 1, 'semester': 1}"),
        @CompoundIndex(name = "subject_idx", def = "{'subject': 1}")
})
public class Note {

    @Id
    private String id;

    private String title;

    private String subject;

    private String branch;

    private Integer semester;

    private String description;

    @Indexed
    private String uploaderId;

    private String uploaderName;

    @Builder.Default
    private List<NoteFileItem> files = new ArrayList<>();

    @Builder.Default
    private long totalSize = 0L;

    @Builder.Default
    private int downloadCount = 0;

    @Builder.Default
    private int bookmarkCount = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
