package com.aaron.usermanagement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvalidatedToken {
    @Id
    String id;          // Sử dụng JTI (JWT ID) của Token làm Khóa chính
    Date expiryTime;    // Thời gian hết hạn của Token để phục vụ việc xóa tự động sau này
}
