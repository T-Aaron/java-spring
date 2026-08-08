package com.aaron.usermanagement.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {

    @Size(min = 3, message = "USERNAME_INVALID") // it nhất 3 ký tự
    @NotBlank(message = "REQUIRED")
    String username;

    @Size(min = 6, message = "INVALID_PASSWORD") // it nhất 3 ký tự
    @NotBlank(message = "REQUIRED")
    String password;

//    @NotBlank(message = "REQUIRED")
    String name;

    @Min(value = 18, message = "AGE_INVALID")
    Integer age;

    // 🌟 Nâng cấp từ String role thành một Set chứa tên các vai trò (Ví dụ: ["USER", "ADMIN"])
    //String role;
    Set<String> roles;
}
