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
