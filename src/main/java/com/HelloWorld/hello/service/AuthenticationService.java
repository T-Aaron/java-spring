package com.HelloWorld.hello.service;

import com.HelloWorld.hello.dto.request.AuthenticationRequest;
import com.HelloWorld.hello.dto.request.IntrospectRequest;
import com.HelloWorld.hello.dto.response.AuthenticationResponse;
import com.HelloWorld.hello.dto.response.IntrospectResponse;
import com.HelloWorld.hello.entity.User;
import com.HelloWorld.hello.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.NonFinal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;

    // TIÊM PasswordEncoder vào đây để dùng chung (Phải được định nghĩa @Bean trong SecurityConfig)
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SINGER_KEY;


    // Logic Kiểm tra Token (Introspect)
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        try {
            // 1. Tạo Verifier (Người kiểm duyệt) dựa trên Key bí mật của bạn
            JWSVerifier verifier = new MACVerifier(SINGER_KEY.getBytes());
            // 2. Giải mã chuỗi Token khách gửi lên thành đối tượng SignedJWT
            SignedJWT signedJWT = SignedJWT.parse(token);
            // 3. Kiểm tra xem chữ ký có khớp không
            boolean verified = signedJWT.verify(verifier);
            // 4. Kiểm tra xem Token đã hết hạn (Expiration Time) chưa
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean isNotExpired = expiryTime.after(new Date());

            return IntrospectResponse.builder()
                    .valid(verified && isNotExpired)
                    .build();
        } catch (JOSEException | ParseException e) {
            // Nếu lỗi parse hoặc verify, nghĩa là token không hợp lệ
            return IntrospectResponse.builder().valid(false).build();
        }


    }

    // 2. Logic Đăng nhập (Authenticate)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // ... logic kiểm tra user ...
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated){
            throw new RuntimeException("Unauthenticated");
        }

        // FIX: Truyền nguyên object user vào, không phải truyền String username
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    //Logic tạo token
    public String generateToken(User user){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()) // Lấy username từ object user
                .issuer("user.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                ))
                // Đây là phần quan quan trọng nhất cho bài học tiếp theo:
                .claim("scope", user.getRole())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SINGER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            // Log lỗi nếu ký Token thất bại
            throw new RuntimeException("Chưa thể tạo Token", e);
        }
    }
}
