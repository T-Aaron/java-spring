package com.aaron.usermanagement.user.dto;

import com.aaron.usermanagement.address.dto.AddressResponse;
import com.aaron.usermanagement.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String name;
    Integer age;
    String role;
    Set<RoleResponse> roles; // 🌟 Nâng cấp thành tập hợp RoleResponse
    // ✅ Khởi tạo sẵn ArrayList rỗng để tránh trả về null cho Frontend

    List<AddressResponse> addresses = new ArrayList<>();

//      Tạo một UserResponse từ một đối tượng User có sẵn mà không cần phải khởi tạo new UserResponse() trước đó ở khắp mọi nơi trong Service.
//      UserResponse.from(user). Nó giống như một chiếc máy đúc, bạn đưa "nguyên liệu" (Entity) vào một đầu, đầu kia nó nhả ra "sản phẩm" (DTO).
    public static UserResponse from(User user) {
        if (user == null) return null;

        Set<RoleResponse> roleResponses = null;
        // 1. Ánh xạ danh sách Roles & Permissions liên kết
        if (!CollectionUtils.isEmpty(user.getRoles())){
            roleResponses = user.getRoles().stream()
                    .map(role -> RoleResponse.builder()
                            .name(role.getName())
                            .description(role.getDescription())
                            .permissions(CollectionUtils.isEmpty(role.getPermissions()) ? null :
                                    role.getPermissions().stream()
                                            .map(p -> PermissionResponse.builder()
                                                    .name(p.getName())
                                                    .description(p.getDescription())
                                                    .build())
                                            .collect(Collectors.toSet()))
                            .build())
                    .collect(Collectors.toSet());
        }


        // 2. Build đối tượng UserResponse hoàn chỉnh
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .age(user.getAge())
//                .role(user.getRole())
                .roles(roleResponses)
                .addresses(user.getAddresses() != null ?
                        user.getAddresses().stream()
                                .map(AddressResponse::from)
                                .toList()
                                : new ArrayList<>()) // ✅ khởi tạo ArrayList rỗng nếu addresses bị null!
                .build();

        // 2. Chuyển đổi các trường đơn giản (Primitive/Wrapper)
        // Chúng ta lấy dữ liệu "thô" từ Database Entity sang "vỏ" Response
//        res.id = user.getId();
//        res.name = user.getName();
//        res.age = user.getAge();

        // Map list Address (Entity) sang AddressResponse (DTO)
        // 3. Xử lý quan hệ (List Address) - ĐOẠN NÀY LÀ QUAN TRỌNG NHẤT
//        if (user.getAddresses() != null) {
//            res.addresses = user.getAddresses().stream()
//                    .map(AddressResponse::from)
//                    .toList();
//        }else {
//            res.addresses = new ArrayList<>();
//        }
//        return res;

//      Hãy tưởng tượng nếu bạn không dùng AddressResponse::from mà trả về trực tiếp List<Address> từ Entity:
//
//      Lỗi vòng lặp (StackOverflow): User gọi Address, Address lại chứa User, User lại gọi Address...
//      Server của bạn sẽ treo ngay lập tức vì Jackson không biết dừng lại ở đâu khi render JSON.
//
//      Bảo mật: Entity Address có thể chứa những thông tin nhạy cảm hoặc không cần thiết (như created_at, updated_at, hay chính User user).
//      DTO giúp bạn chắt lọc chỉ lấy city và street.
    }

}
