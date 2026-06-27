package com.scholario.violation.repository;

import com.scholario.violation.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    List<AccessLog> findByUsername(String username);
    
    @Query("SELECT a.username, COUNT(a) FROM AccessLog a WHERE a.allowed = false AND a.timestamp > :since GROUP BY a.username HAVING COUNT(a) > :threshold")
    List<Object[]> findUsersWithExcessiveDeniedAccess(@Param("since") LocalDateTime since, @Param("threshold") long threshold);

    @Query("SELECT a.username, COUNT(a) FROM AccessLog a WHERE a.timestamp > :since GROUP BY a.username HAVING COUNT(a) > :threshold")
    List<Object[]> findUsersWithExcessiveActivity(@Param("since") LocalDateTime since, @Param("threshold") long threshold);
}
