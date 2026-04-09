package com.HelloWorld.hello.user;

import com.HelloWorld.hello.exception.UserNotFoundException;
import com.HelloWorld.hello.entity.User;
import com.HelloWorld.hello.repository.UserRepository;
import com.HelloWorld.hello.user.dto.UserRequest;
import com.HelloWorld.hello.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    //create
    public UserResponse create(@Valid UserRequest request){
        User user = new User();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setAge(request.getAge());

        // 🛡️ Mã hóa mật khẩu trước khi lưu vào DB
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);
        return UserResponse.from(saved); // Trả về DTO thay vì Entity
    }

    //getAll
    public List<UserResponse> getUsers(){
        // Nên đặt tên biến rõ ràng để dễ debug nếu cần
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(UserResponse::from)
                .toList();
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

    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setRole(request.getRole());

        User saved = userRepository.save(user);

        return UserResponse.from(saved);
    }

    public UserResponse getById(Long id){
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

}
