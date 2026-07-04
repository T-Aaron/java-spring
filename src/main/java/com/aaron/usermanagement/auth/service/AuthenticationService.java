package com.aaron.usermanagement.auth.service;

import com.aaron.usermanagement.auth.InvalidatedToken;
import com.aaron.usermanagement.auth.InvalidatedTokenRepository;
import com.aaron.usermanagement.auth.dto.AuthenticationRequest;
import com.aaron.usermanagement.auth.dto.IntrospectRequest;
import com.aaron.usermanagement.auth.dto.RefreshTokenRequest;
import com.aaron.usermanagement.auth.dto.AuthenticationResponse;
import com.aaron.usermanagement.auth.dto.IntrospectResponse;
import com.aaron.usermanagement.user.User;
import com.aaron.usermanagement.user.UserRepository;
import com.aaron.usermanagement.auth.dto.LogoutRequest;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.NonFinal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {

    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    // TIÊM PasswordEncoder vào đây để dùng chung (Phải được định nghĩa @Bean trong SecurityConfig)
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SINGER_KEY;

    //Logic xác minh token có hợp lệ hoặc hết hạn
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
            throw new RuntimeException("UNAUTHENTICATED");
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

    // Logic Đăng nhập (Authenticate)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // ... logic kiểm tra user ...
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated){
            throw new RuntimeException("Unauthenticated");
        }

        // FIX: Truyền nguyên object user vào, không phải truyền String username
        // Tạo đồng thời Access Token (Hạn ngắn) và Refresh Token (Hạn dài)
//       var token = generateToken(user);
        var accessToken = generateAccessToken(user);
        var refreshToken = generateRefreshToken(user);

        return AuthenticationResponse.builder()
//                .token(token)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    // -----------------------------------
    //    LOGIC LÀM MỚI TOKEN (Refresh Token với cơ chế Token Rotation)

    public AuthenticationResponse refreshToken (RefreshTokenRequest request) throws JOSEException, ParseException {
        // 1. Xác thực Refresh Token gửi lên có hợp lệ không
        SignedJWT signedJWT = verifyToken(request.getRefreshToken());

        // 2. Trích xuất ID (jti) và thời gian hết hạn của Refresh Token cũ
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // 3. Kiểm tra xem Refresh Token này đã bị đưa vào danh sách đen chưa
        if (invalidatedTokenRepository.existsById(jti)){
            throw new RuntimeException("TOKEN_INVALIDATED");
        }

        // 4. Khai tử ngay lập tức Refresh Token cũ bằng cách đưa vào bảng Blacklist
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        // 5. Trích xuất thông tin User để cấp phiên đăng nhập mới
        String username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 6. Tạo cặp Token mới tinh (AccessToken mới + RefreshToken mới)
        var accessToken = generateAccessToken(user);
        var refreshToken = generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    // 1. Hàm tạo ACCESS TOKEN (Hạn ngắn - 10 phút)
    public String generateAccessToken(User user){
        return generateToken(user, 600000); // 10 phút = 600,000 ms
    }

    // 2. Hàm tạo REFRESH TOKEN (Hạn dài - 30 ngày)
    public String generateRefreshToken(User user){
        // Refresh Token không cần scope/roles để giảm dung lượng, chỉ cần Username và JTI để quản lý
        return generateToken(user, 2592000000L); // 30 ngày = 2,592,000,000 ms
    }
    //-------------------------------------------------
    // Logic tạo token
    // Hàm tạo Token gốc được tối ưu lại (Sử dụng thời gian linh hoạt)
    public String generateToken(User user, long expiryDuration){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()) // Lấy username từ object user
                .issuer("user.com")
                .issueTime(new Date())
//              .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .expirationTime(new Date(System.currentTimeMillis() + expiryDuration))
                // Quan trọng:
                .jwtID(UUID.randomUUID().toString())  //Cấp ID độc nhất cho Token. Quan trọng để Blacklist sau này
//              .claim("scope", user.getRole())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SINGER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            // Log lỗi nếu ký Token thất bại
            log.error("Không thể tạo token", e);
            throw new RuntimeException(e);
        }
    }

    //Logic đăng xuất
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

    // HÀM BUILD SCOPE ĐỘNG: Chuyển role của User thành chuỗi Scope cho JWT

    private String buildScope(User user){
//        if (user.getRole() != null && !user.getRole().isEmpty()){
//            return user.getRole();  // Trả về "ADMIN" hoặc "USER" trực tiếp từ Entity
//        }
//        return "";

    //HÀM BUILD SCOPE ĐỘNG NÂNG CẤP: Gộp tất cả Roles và Permissions của User
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                // 1. Thêm vai trò với tiền tố "ROLE_" (Ví dụ: ROLE_ADMIN)
                stringJoiner.add("ROLE_" + role.getName());

                // 2. Thêm toàn bộ các quyền hạn cụ thể (Ví dụ: CREATE_DATA, DELETE_USER)
                if (!CollectionUtils.isEmpty(user.getRoles())){
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString();
    }




}
