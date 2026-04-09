package com.HelloWorld.hello.address;

import com.HelloWorld.hello.address.dto.AddressRequest;
import com.HelloWorld.hello.address.dto.AddressResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//logic: Nhìn vào URL, ai cũng hiểu là bạn đang "tạo một địa chỉ cho User có ID này"
@RequestMapping("/api/users/{userId}/addresses") //URL cấu trúc cha con
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @PathVariable Long userId,
            @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.ok(addressService.create(userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        addressService.delete(id);
        return ResponseEntity.ok("Address deleted successfully");
    }
}
