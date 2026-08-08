package com.aaron.usermanagement.security;

import com.aaron.usermanagement.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    @Autowired
    private UserRepository userRepository;

    // 🛡️ Hàm Helper dùng cho @PreAuthorize giúp xác minh xem user_id gửi lên có phải là chính chủ đang gọi API hay không
    public boolean isOwner(Long userId) {
        if (userId == null) {
            return false;
        }

        var context = SecurityContextHolder.getContext();
        if (context.getAuthentication() == null) {
            return false;
        }

        String currentUsername = context.getAuthentication().getName();

        return userRepository.findById(userId)
                .map(user -> user.getUsername().equals(currentUsername))
                .orElse(false);
    }
}
