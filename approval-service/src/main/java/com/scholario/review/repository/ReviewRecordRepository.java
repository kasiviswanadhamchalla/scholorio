package com.scholario.review.repository;

import com.scholario.review.model.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {
    Optional<ReviewRecord> findByBookId(Long bookId);
}
