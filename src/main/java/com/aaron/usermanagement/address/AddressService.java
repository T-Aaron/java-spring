package com.aaron.usermanagement.address;

import com.aaron.usermanagement.address.dto.AddressRequest;
import com.aaron.usermanagement.address.dto.AddressResponse;
import com.aaron.usermanagement.entity.Address;
import com.aaron.usermanagement.exception.UserNotFoundException;
import com.aaron.usermanagement.repository.AddressRepository;
import com.aaron.usermanagement.repository.UserRepository;
import com.aaron.usermanagement.entity.User;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository; // Cần thêm repo này để tìm User

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    //create
    @Transactional
    public AddressResponse create(Long userId, @Valid AddressRequest request) {
        // 1. Tìm User xem có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. Map data
        Address address = new Address();
        address.setCity(request.getCity());
        address.setStreet(request.getStreet());

        // ✅ ĐÚNG: Thiết lập mối quan hệ (Khóa ngoại user_id sẽ tự động được tạo)
        // 3. Liên kết địa chỉ với User
        address.setUser(user);
        // ID của address hãy để Database tự sinh (GenerationType.IDENTITY)
        // Tuyệt đối KHÔNG gọi address.setId(...)

        // 4. Lưu và trả về DTO
        Address saved = addressRepository.save(address);
        return AddressResponse.from(saved);
    }

    //delete
    @Transactional
    public void delete (Long id){
        if (!addressRepository.existsById(id)){
            throw new RuntimeException("Address not found with id:" + id);
        }

        addressRepository.deleteById(id);
    }

}
