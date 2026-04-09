package com.HelloWorld.hello.user.dto;

import com.HelloWorld.hello.address.dto.AddressResponse;
import com.HelloWorld.hello.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

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

    // ✅ Khởi tạo sẵn ArrayList rỗng để tránh trả về null cho Frontend
    @Builder.Default // Giúp Builder khởi tạo sẵn list rỗng thay vì null
    private List<AddressResponse> addresses = new ArrayList<>();

    // Constructor nên dùng Wrapper Long để đồng bộ, thêm address
    public UserResponse(long id, String name, Integer age, String role, List<AddressResponse> addresses) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.role = role;
        this.addresses = addresses != null ? addresses : new ArrayList<>();
    }

//      Tạo một UserResponse từ một đối tượng User có sẵn mà không cần phải khởi tạo new UserResponse() trước đó ở khắp mọi nơi trong Service.
//      UserResponse.from(user). Nó giống như một chiếc máy đúc, bạn đưa "nguyên liệu" (Entity) vào một đầu, đầu kia nó nhả ra "sản phẩm" (DTO).
    public static UserResponse from(User user) {
        // 1. Khởi tạo đối tượng DTO rỗng
//        UserResponse res = new UserResponse();
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .age(user.getAge())
                .role(user.getRole())
                .addresses(user.getAddresses() != null ?
                        user.getAddresses().stream()
                                .map(AddressResponse::from)
                                .toList()
                                : new ArrayList<>())
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
