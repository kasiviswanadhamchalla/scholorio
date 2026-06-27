package com.scholario.content.repository;

import com.scholario.content.model.UserContentAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserContentAccessRepository extends JpaRepository<UserContentAccess, Long> {
    Optional<UserContentAccess> findByUserIdAndContentId(Long userId, Long contentId);
    boolean existsByUserIdAndContentId(Long userId, Long contentId);
    void deleteByUserIdAndContentId(Long userId, Long contentId);
}
