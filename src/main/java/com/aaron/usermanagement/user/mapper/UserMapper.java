package com.aaron.usermanagement.user.mapper;

import com.aaron.usermanagement.entity.User;
import com.aaron.usermanagement.user.dto.UserRequest;
import com.aaron.usermanagement.user.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel  = "spring") // Báo cho Spring biết đây là một Bean cần quản lý

public interface UserMapper {
    User toUser(UserRequest request);
    UserResponse toUserResponse(User user);
}
