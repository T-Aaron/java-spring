package com.aaron.usermanagement.user;

import com.aaron.usermanagement.dto.ApiResponse;
import com.aaron.usermanagement.user.dto.UserRequest;
import com.aaron.usermanagement.user.dto.UserResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    //GET all
//    public ResponseEntity<List<UserResponse>> getAll(){
//        return ResponseEntity.ok(userService.getAll());
//    }

    // 1. Endpoint đăng ký tự do công khai (Chống leo thang đặc quyền!)
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.register(request))
                .build();
    }

    // 2. Endpoint tạo user có phân quyền (Chỉ dành cho ADMIN)
    @PostMapping
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.create(request))
                .build();
    }

    // 3. Endpoint lấy danh sách users (Chỉ dành cho ADMIN)
    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers(){
        return ApiResponse.<List<UserResponse>>builder() // cài Plugin Lombok: Đảm bảo bạn đã cài plugin Lombok trong IntelliJ.
                .result(userService.getUsers())
                .build();
    }

    // 4. Lấy chi tiết user (Đã có @PreAuthorize chống IDOR ở tầng Service)
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getById(id))
                .build();
    }

    // 5. Cập nhật thông tin user (Đã có @PreAuthorize chống IDOR ở tầng Service)
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request){
        UserResponse updated = userService.update(id, request);
        return ApiResponse.<UserResponse>builder()
                .result(updated)
                .build();
    }

    // 6. Xóa user (Chỉ dành cho ADMIN)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete (@PathVariable Long id){
        userService.deleteUserById(id);
        return ApiResponse.<Void>builder()
                .message("User has been deleted successfully")
                .build();
    }

    // 7. Endpoint lấy thông tin cá nhân của người dùng hiện tại
    @GetMapping("/my-info")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    //Search
    @GetMapping("/search")
    public ApiResponse<List<UserResponse>> search(@RequestParam String keyword){
        List<UserResponse> user = userService.searchByName(keyword);
//        return ResponseEntity.ok(user);
        return ApiResponse.<List<UserResponse>>builder()
                .result(user)
                .build();
    }
}
