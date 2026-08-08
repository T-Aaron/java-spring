package com.aaron.usermanagement.user;

import com.aaron.usermanagement.exception.AppException;
import com.aaron.usermanagement.exception.ErrorCode;
import com.aaron.usermanagement.exception.UserNotFoundException;
import com.aaron.usermanagement.rbac.Role;
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
    PasswordEncoder passwordEncoder;


    // 1. Đăng ký tài khoản tự do (Register) - KHÔNG nhận role từ client, luôn luôn là USER
    public UserResponse register(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Gán cứng quyền USER để chống Privilege Escalation
        var defaultRole = roleRepository.findById("USER")
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        var roles = new HashSet<Role>();
        roles.add(defaultRole);
        user.setRoles(roles);

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    // 2. Admin chủ động tạo mới người dùng (ADMIN Only)
    @PreAuthorize("hasRole('ADMIN')")
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

    // 3. Lấy tất cả người dùng (ADMIN Only)
    @PreAuthorize("hasRole('ADMIN')") //ROLE_ADMIN mới vào được hàm này
    public List<UserResponse> getUsers(){
        // Nên đặt tên biến rõ ràng để dễ debug nếu cần
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserResponse::from)
                .toList();
    }

    // 4. Lấy thông tin cá nhân theo ID (Chống IDOR - Chỉ ADMIN hoặc chính chủ ID đó mới xem được)
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public UserResponse getById(Long id){
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // 5. Cập nhật thông tin người dùng (Update)
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public UserResponse update(Long id, UserRequest request) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//
//        user.setName(request.getName());
//        user.setAge(request.getAge());
//        // Chỉ ADMIN mới có quyền nâng cấp/thay đổi roles
//        var context = SecurityContextHolder.getContext();
//        boolean isAdmin = context.getAuthentication().getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//
//        if (isAdmin && request.getRoles() != null) {
//            var roles = roleRepository.findAllById(request.getRoles());
//            user.setRoles(new HashSet<>(roles));
//        }
//
//        User updated = userRepository.save(user);
//
//        return UserResponse.from(updated);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Chỉ cập nhật khi thông tin gửi lên khác NULL (Giữ nguyên dữ liệu cũ)
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }

        // 2. Kiểm tra quyền ADMIN
        var context = SecurityContextHolder.getContext();
        boolean isAdmin = context.getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ADMIN")); // Check cả 2 dạng cho an toàn

        // 3. Chỉ ADMIN mới được phép thay đổi roles VÀ phải có dữ liệu roles gửi lên
        if (isAdmin && request.getRoles() != null && !request.getRoles().isEmpty()) {
            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        User updated = userRepository.save(user);

        return UserResponse.from(updated);
    }



    //  👉 Service chỉ nói sự thật: có hoặc không.
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }


    public UserResponse getMyInfo(){
        // 1. Vào SecurityContext để bốc ra tên người dùng (username) đang đăng nhập từ JWT Token
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        // 2. Tìm User trong DB bằng username đó, nếu thấy thì map sang DTO, không thấy ném lỗi
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
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





}
