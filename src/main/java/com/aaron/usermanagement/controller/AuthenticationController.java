package com.aaron.usermanagement.controller;

import com.aaron.usermanagement.dto.request.IntrospectRequest;
import com.aaron.usermanagement.dto.request.AuthenticationRequest;
import com.aaron.usermanagement.dto.request.RefreshTokenRequest;
import com.aaron.usermanagement.dto.response.ApiResponse;
import com.aaron.usermanagement.dto.response.AuthenticationResponse;
import com.aaron.usermanagement.dto.response.IntrospectResponse;
import com.aaron.usermanagement.service.AuthenticationService;
import com.aaron.usermanagement.user.dto.LogoutRequest;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.text.ParseException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
//    public String login(@RequestBody AuthenticationRequest request ){
//        // Tạm thời bỏ qua bước check password, cứ login là cấp token
//        return authenticationService.generateToken(request.getUsername());
//    }
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
//    public boolean introspect (@RequestBody IntrospectRequest request) throws JOSEException, ParseException {
//        return authenticationService.introspect(request.getToken());
//    }
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
        throws JOSEException, ParseException{

        var result = authenticationService.introspect(request);

//        ApiResponse<IntrospectResponse> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(result);
//        return apiResponse;
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException{
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .message("Logout successfully")
                .build();
    }

    // GIA HẠN ĐĂNG NHẬP (Tự động cấp Access Token mới)
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request)
            throws JOSEException, ParseException{
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }
}
