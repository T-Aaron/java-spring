package com.aaron.usermanagement.user;

import com.aaron.usermanagement.exception.UserNotFoundException;
import com.aaron.usermanagement.rbac.RoleRepository;
import com.aaron.usermanagement.user.dto.UserRequest;
import com.aaron.usermanagement.user.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
//    UserMapper userMapper;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //create
    public UserResponse create(@Valid UserRequest request){
        User user = new User();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setAge(request.getAge());

        // 🛡️ Mã hóa mật khẩu trước khi lưu vào DB
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        //Chuyển đổi Set<String> tên Role thành Set<Role> thực thể trong DB
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        User saved = userRepository.save(user);
        return UserResponse.from(saved); // Trả về DTO thay vì Entity
    }

    //getAll
    @PreAuthorize("hasRole('ADMIN')") //ROLE_ADMIN mới vào được hàm này
    public List<UserResponse> getUsers(){
        // Nên đặt tên biến rõ ràng để dễ debug nếu cần
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserResponse::from)
                .toList();
    }

    // 3. Lấy thông tin chi tiết qua Username (Get By Username)
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #username")
    // Giải thích: Nếu là ADMIN thì OK, nếu không thì username gửi lên phải khớp với username trong Token
    public UserResponse getUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.from(user);

    }
    //Delete by id
//    public boolean deleteUserById(long id){
//        if (!userRepository.existsById(id)){
//            return false;
//        }
//        userRepository.deleteById(id);
//        return true;
//    }

//  👉 Service chỉ nói sự thật: có hoặc không.
    public void deleteUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }

    //Search by keyword
    public List<UserResponse> searchByName(String keyword){
        List<User> users = userRepository.findByNameContainingIgnoreCase(keyword);
//        if (users.isEmpty()) {
//            throw new UserNotFoundException(keyword);
//        }
        // Chuyển đổi List<User> sang List<UserResponse>
        return users.stream().map(UserResponse::from).toList();
    }

    // 6. Cập nhật thông tin người dùng (Update)
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setName(request.getName());
        user.setAge(request.getAge());
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        User saved = userRepository.save(user);

        return UserResponse.from(saved);
    }

    public UserResponse getById(Long id){
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public UserResponse getMyInfo(){
        // 1. Vào SecurityContext để bốc ra tên người dùng (username) đang đăng nhập từ JWT Token
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        // 2. Tìm User trong DB bằng username đó, nếu thấy thì map sang DTO, không thấy ném lỗi
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.from(user);
    }

}
