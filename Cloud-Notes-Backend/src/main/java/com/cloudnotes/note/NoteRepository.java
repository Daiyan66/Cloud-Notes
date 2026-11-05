package com.cloudnotes.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    Page<Note> findByUploaderId(String uploaderId, Pageable pageable);

    List<Note> findByUploaderId(String uploaderId);

    long countByUploaderId(String uploaderId);

    long countBySemester(Integer semester);

    long countByBranch(String branch);
}
