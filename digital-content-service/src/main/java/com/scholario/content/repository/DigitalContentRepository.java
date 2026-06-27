package com.scholario.content.repository;

import com.scholario.content.model.DigitalContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DigitalContentRepository extends JpaRepository<DigitalContent, Long> {
    List<DigitalContent> findByBookId(Long bookId);
}
