# Java Spring Boot - Role-Based Access Control (RBAC)

Dự án thực hành xây dựng hệ thống quản lý người dùng với phân quyền (ADMIN/USER).

### Công nghệ sử dụng:
* **Java 21**, **Spring Boot 3**
* **Spring Security & JWT (Nimbus JOSE + JWT)**
* **MySQL Database**, **Spring Data JPA**, **Lombok**

## ⚙️ Kiến trúc & Luồng xử lý Tính năng (Flowcharts)

### 1. Tính năng: Xác thực JWT & Phân quyền tĩnh (RBAC)
Luồng đi của một Request yêu cầu tài nguyên được bảo mật (Ví dụ: Các API quản lý người dùng của ADMIN).

```mermaid
graph TD
    A[Client Request] -->|Gửi kèm Authorization: Bearer Token| B(JwtAuthenticationFilter)
    B --> C{JwtDecoder giải mã chữ ký}
    C -->|Token Giả/Sai Chữ Ký/Hết Hạn| D[Trả về lỗi 401 Unauthorized]
    C -->|Token Hợp Lệ| E[JwtAuthenticationConverter]
    E -->|Thêm tiền tố ROLE_| F[Nạp Quyền vào SecurityContextHolder]
    F --> G{Kiểm tra @PreAuthorize trên Service}
    G -->|Không đúng Role| H[GlobalExceptionHandler bắt AccessDeniedException - Mã 1006]
    G -->|Đúng Quyền ADMIN/USER| I[Xử lý Logic & Trả về dữ liệu thành công]


graph LR
    A[Client Request GET /api/users/my-info] --> B(SecurityContextHolder)
    B -->|Bóc tách trích xuất| C[Get Name -> Lấy ra Username]
    C --> D(UserRepository)
    D -->|findByUsername| E{Tìm thấy trong DB?}
    E -->|No| F[Ném lỗi User Not Found]
    E -->|Yes| G[UserMapper chuyển sang UserResponse]
    G --> H[Trả về dữ liệu cá nhân 200 OK]

graph TD
    A[Client Request POST /auth/logout] --> B(AuthenticationService)
    B --> C(verifyToken)
    C -->|Token rác/Hết hạn sẵn| D[Bỏ qua - Kết thúc xử lý]
    C -->|Token Hợp Lệ| E[Trích xuất JTI ID & Expiration Time]
    E --> F[Khởi tạo Object InvalidatedToken]
    F --> G(InvalidatedTokenRepository)
    G -->|Lưu vào MySQL| H[(Bảng invalidated_token)]
    
    %% Luồng chặn Request sau khi Logout %%
    I[Request tiếp theo gửi tới cửa ngõ] --> J(Custom JwtDecoder)
    J -->|Đọc JTI của Token| K{Có tồn tại trong bảng danh sách đen?}
    K -->|Yes| L[Ném lỗi JwtException -> Chặn đứng trả về 401]
    K -->|No| M[Cho phép đi tiếp vào hệ thống]

---
