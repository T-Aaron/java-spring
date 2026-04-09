package com.HelloWorld.hello.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter // Chỉ tạo Getter
@Setter // Chỉ tạo Setter
@Builder // Hỗ trợ tạo object nhanh: User.builder().username("...").build()
@NoArgsConstructor // Constructor không đối số (Bắt buộc cho JPA)
@AllArgsConstructor // Constructor đầy đủ đối số
@FieldDefaults(level = AccessLevel.PRIVATE) // Tự động đặt private cho các field
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // ✅ BẮT BUỘC PHẢI CÓ ĐỂ LOGIN
    @Column(unique = true, nullable = false) // Đảm bảo username không trùng và không trống
    String username;

    @Column(nullable = false)
    String password;
    String name;
    Integer age;
    String role;

    @ToString.Exclude // <--- Cực kỳ quan trọng: Ngăn vòng lặp khi in log
    @EqualsAndHashCode.Exclude // <--- Ngăn vòng lặp khi so sánh object
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>(); // Khởi tạo sẵn để tránh NullPointerException

//    Khi làm việc với quan hệ Hai chiều (Bidirectional),
//    các Mentor thường viết thêm một hàm "tiện ích" để đảm bảo khi thêm một địa chỉ vào User,
//    thì đối tượng Address đó cũng tự động nhận User làm cha.
    public void addAddress(Address address){
        addresses.add(address);
        address.setUser(this); // Thiết lập mối quan hệ 2 chiều ngay lập tức
    }


    // ADDRESS
    //Để Mapping sang DTO: Như Coach đã dặn ở bài trước, bạn cần lấy danh sách Address từ User để chuyển đổi sang AddressResponse.
    // Nếu không có Getter, bạn không thể truy cập danh sách này từ lớp Service hoặc Response.

    //Để Jackson Serialization: Khi Spring Boot trả về dữ liệu,
//  //nó quét các hàm bắt đầu bằng get để tạo ra các trường JSON tương ứng.
//    public List<Address> getAddresses(){
//        return addresses;
//    }
//
//    public void setAddresses(List<Address> addresses){
//        this.addresses = addresses;
//    }
}
