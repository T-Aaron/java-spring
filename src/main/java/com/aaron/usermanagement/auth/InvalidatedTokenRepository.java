package com.aaron.usermanagement.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    // Xóa tất cả các token có expiryTime nhỏ hơn (trước) thời gian truyền vào
    @Transactional
    void deleteAllByExpiryTimeBefore(Date now);
}
