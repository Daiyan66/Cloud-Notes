package com.cloudnotes.paper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionPaperRepository extends MongoRepository<QuestionPaper, String> {
    Page<QuestionPaper> findByUploaderId(String uploaderId, Pageable pageable);
    List<QuestionPaper> findByUploaderId(String uploaderId);
    long countByUploaderId(String uploaderId);
}
