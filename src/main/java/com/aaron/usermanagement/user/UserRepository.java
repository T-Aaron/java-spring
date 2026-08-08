package com.aaron.usermanagement.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByNameContainingIgnoreCase(String keyword);

    // Tự động sinh SQL: SELECT COUNT(*) > 0 FROM users WHERE username = ?
    boolean existsByUsername(String username);

    // ✅ Thêm dòng này để phục vụ logic Login
    Optional<User> findByUsername(String username);
}
