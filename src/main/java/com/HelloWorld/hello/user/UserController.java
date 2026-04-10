package com.HelloWorld.hello.user;

import com.HelloWorld.hello.dto.response.ApiResponse;
import com.HelloWorld.hello.user.dto.UserRequest;
import com.HelloWorld.hello.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    //GET all

//    public ResponseEntity<List<UserResponse>> getAll(){
//        return ResponseEntity.ok(userService.getAll());
//    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserResponse>> getUsers(){
        return ApiResponse.<List<UserResponse>>builder() // cài Plugin Lombok: Đảm bảo bạn đã cài plugin Lombok trong IntelliJ.
                .result(userService.getUsers())
                .build();
    }

    //Get user by id
    @GetMapping("/{id}")
    //@PathVariable: Nếu không có nó, Spring sẽ không biết lấy giá trị từ URL để đổ vào biến
//    public ResponseEntity<UserResponse> getById (@PathVariable Long id){
//        return ResponseEntity.ok(userService.getById(id));
//    }
    public ApiResponse<UserResponse> getById (@PathVariable Long id){
        return ApiResponse.<UserResponse>builder()
                .result(userService.getById(id)).build();
    }

    //Create
    @PostMapping
//    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request){
//        return ResponseEntity.ok(userService.create(request));
//    }
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request){
        UserResponse updated = userService.update(id, request);
        return ApiResponse.<UserResponse>builder()
                .result(updated)
                .build();
    }

    //Delete
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete (@PathVariable Long id){
//        boolean deleted = userService.deleteUserById(id);
//        if (!deleted){
//            return ResponseEntity.status(404).body("User not found");
//        }
//      3️⃣ Controller KHÔNG xử lý lỗi nữa
//      👉 Controller sạch.
        userService.deleteUserById(id);
        return ApiResponse.<String>builder()
                .result("User has been deleted")
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
