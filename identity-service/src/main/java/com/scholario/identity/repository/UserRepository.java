package com.scholario.identity.repository;

import com.scholario.identity.model.Role;
import com.scholario.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRoles(Role role);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
