package com.scholario.royalty.repository;

import com.scholario.royalty.model.RoyaltyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoyaltyRecordRepository extends JpaRepository<RoyaltyRecord, Long> {
    List<RoyaltyRecord> findByBookId(Long bookId);
    List<RoyaltyRecord> findByFacultyId(Long facultyId);
    List<RoyaltyRecord> findByPayoutStatus(String payoutStatus);
}
