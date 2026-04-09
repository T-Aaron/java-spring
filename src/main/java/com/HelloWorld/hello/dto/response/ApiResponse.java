package com.HelloWorld.hello.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // 💡 Chỉ hiện những trường có dữ liệu
public class ApiResponse <T> {
    @Builder.Default
    int code = 1000; // 1000 là mã mặc định cho "Thành công"
    String message;
    T result;
}
