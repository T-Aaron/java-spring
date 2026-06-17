package com.HelloWorld.hello.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshTokenRequest {
    String refreshToken; // Chuỗi Token hạn dài do Client truyền lên để xin cấp mới
}
