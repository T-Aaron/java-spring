package com.HelloWorld.hello.service;

import com.HelloWorld.hello.dto.request.AuthenticationRequest;
import com.HelloWorld.hello.dto.request.IntrospectRequest;
import com.HelloWorld.hello.dto.response.AuthenticationResponse;
import com.HelloWorld.hello.dto.response.IntrospectResponse;
import com.HelloWorld.hello.entity.InvalidatedToken;
import com.HelloWorld.hello.entity.User;
import com.HelloWorld.hello.repository.InvalidatedTokenRepository;
import com.HelloWorld.hello.repository.UserRepository;
import com.HelloWorld.hello.user.dto.LogoutRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;

    // TIÊM PasswordEncoder vào đây để dùng chung (Phải được định nghĩa @Bean trong SecurityConfig)
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SINGER_KEY;

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException{
        JWSVerifier verifier = new MACVerifier(SINGER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        // 1. Kiểm tra chữ ký hợp lệ
        boolean verified = signedJWT.verify(verifier);

        // 2. Kiểm tra thời gian hết hạn
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean isNotExpried = expiryTime != null && expiryTime.after(new Date());

        // Nếu chữ ký sai HOẶC token đã hết hạn trước đó rồi -> Coi như không hợp lệ
        if (!verified || !isNotExpried){
            throw new RuntimeException("Token invalid or expried");
        }

        return signedJWT;
    }

    // Logic Kiểm tra Token (Introspect)
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        try {
//            // 1. Tạo Verifier (Người kiểm duyệt) dựa trên Key bí mật của bạn
//            JWSVerifier verifier = new MACVerifier(SINGER_KEY.getBytes());
//            // 2. Giải mã chuỗi Token khách gửi lên thành đối tượng SignedJWT
//            SignedJWT signedJWT = SignedJWT.parse(token);
//            // 3. Kiểm tra xem chữ ký có khớp không
//            boolean verified = signedJWT.verify(verifier);
//            // 4. Kiểm tra xem Token đã hết hạn (Expiration Time) chưa
//            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//            boolean isNotExpired = expiryTime.after(new Date());

            verifyToken(request.getToken());
            return IntrospectResponse.builder().valid(true).build();

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
                .jwtID(UUID.randomUUID().toString()) // 🌟 THÊM DÒNG NÀY: Cấp ID độc nhất cho Token
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

    public void logout(LogoutRequest request) throws  ParseException, JOSEException{
        try {
            String token = request.getToken();

            // 1. Ép đi qua hàm xác thực chữ ký và hạn dùng (Chống spam DB)
            SignedJWT signedJWT = verifyToken(token);

            // có thể dùng hàm verify token có sẵn của bạn ở đây
            // Nếu token giả mạo hoặc lỗi thì sẽ ném ra Exception ngay

            // 2. Bốc tách ID (jti) và thời gian hết hạn (expiryTime) từ Token ra
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            // 3. Lưu thông tin Token bị hủy vào Database
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jti)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (Exception e) {
            // Nếu token đã sai hoặc hết hạn sẵn rồi thì không cần làm gì cả, xem như logout xong
            // Log lỗi nhẹ ra console nếu cần thiết
        }

    }
}
