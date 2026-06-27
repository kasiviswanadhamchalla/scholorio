package com.scholario.lending.repository;

import com.scholario.lending.model.IssueRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {

    List<IssueRecord> findByUserId(Long userId);

    List<IssueRecord> findByBookId(Long bookId);

    List<IssueRecord> findByUserIdAndStateTypeNot(Long userId, String stateType);

    List<IssueRecord> findByStateType(String stateType);

    List<IssueRecord> findByDueDateLessThanAndStateType(LocalDateTime now, String stateType);

    Optional<IssueRecord> findByIdAndUserId(Long id, Long userId);

    long countByBookId(Long bookId);

    long countByUserId(Long userId);

    long countByStateTypeNot(String stateType);

    long countByStateType(String stateType);

    long countByReturnDateAfter(LocalDateTime startOfDay);

    @Query("SELECT i.bookId AS bookId, COUNT(i) AS issueCount FROM IssueRecord i GROUP BY i.bookId")
    List<BookIssueCount> countIssuesByBook();

    interface BookIssueCount {
        Long getBookId();

        long getIssueCount();
    }
}
