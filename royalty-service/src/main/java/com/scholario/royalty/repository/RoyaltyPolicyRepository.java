package com.scholario.royalty.repository;

import com.scholario.royalty.model.RoyaltyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoyaltyPolicyRepository extends JpaRepository<RoyaltyPolicy, Long> {
    Optional<RoyaltyPolicy> findByBookId(Long bookId);
}
