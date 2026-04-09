# Java Spring Boot - Role-Based Access Control (RBAC)

Dự án thực hành xây dựng hệ thống quản lý người dùng với phân quyền (ADMIN/USER).

### Công nghệ sử dụng:
* **Java 21**, **Spring Boot 3**
* **Spring Security & JWT (Nimbus JOSE + JWT)**
* **MySQL Database**, **Spring Data JPA**, **Lombok**

### Tính năng đã hoàn thành:
* Đăng ký/Đăng nhập với mật khẩu được mã hóa (BCrypt).
* Xác thực người dùng bằng JWT Token.
* Phân quyền dựa trên Role (ADMIN có quyền xem danh sách users, USER bị chặn).
