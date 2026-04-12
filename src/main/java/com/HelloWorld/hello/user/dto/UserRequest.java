package com.HelloWorld.hello.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @NotBlank(message = "REQUIRED")
    String name;

    @Min(value = 0, message = "AGE_INVALID")
    Integer age;

    String role;

}
