package com.HelloWorld.hello.repository;

import com.HelloWorld.hello.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByNameContainingIgnoreCase(String keyword);

    // ✅ Thêm dòng này để phục vụ logic Login
    Optional<User> findByUsername(String username);
}
