package com.scholario.review.repository;

import com.scholario.review.model.ReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewHistoryRepository extends JpaRepository<ReviewHistory, Long> {
    List<ReviewHistory> findByReviewRecordIdOrderByTimestampDesc(Long reviewRecordId);
}
