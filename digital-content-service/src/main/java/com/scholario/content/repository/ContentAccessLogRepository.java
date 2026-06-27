package com.scholario.content.repository;

import com.scholario.content.model.ContentAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContentAccessLogRepository extends JpaRepository<ContentAccessLog, Long> {
    List<ContentAccessLog> findByContentId(Long contentId);
    List<ContentAccessLog> findByUserId(Long userId);
    long countByContentIdIn(List<Long> contentIds);
    long countByUserId(Long userId);
}
