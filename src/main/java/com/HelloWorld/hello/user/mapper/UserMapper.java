package com.HelloWorld.hello.user.mapper;

import com.HelloWorld.hello.entity.User;
import com.HelloWorld.hello.user.dto.UserRequest;
import com.HelloWorld.hello.user.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel  = "spring") // Báo cho Spring biết đây là một Bean cần quản lý

public interface UserMapper {
    User toUser(UserRequest request);
    UserResponse toUserResponse(User user);
}
