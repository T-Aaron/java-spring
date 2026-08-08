package com.aaron.usermanagement.security;
import com.aaron.usermanagement.auth.InvalidatedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // <--- Kích hoạt bảo mật tầng Method (@PreAuthorize)
@EnableScheduling // 🌟 Kích hoạt tính năng chạy ngầm theo chu kỳ
public class SecurityConfig {
    @Value("${jwt.signerKey}")
    protected String SINGER_KEY;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 1. Tầng Repository kiểm tra danh sách đen vào cấu hình bảo mật
    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        //Cấu hình JWT.
        http.authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.POST, "/api/users/register").permitAll() //Bạn hãy tạm thời thêm để có thể tạo được user đầu tiên mà không bị chặn (401/403).
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/introspect", "/auth/logout", "/auth/refresh").permitAll() // 🌟 THÊM /auth/logout VÀO ĐÂY
                        .anyRequest().authenticated());

        // Cấu hình để Server đóng vai trò là Resource Server kiểm tra JWT
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );

        http.csrf(AbstractHttpConfigurer :: disable);

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(){
        // Đây là nơi Spring dùng SINGER_KEY để giải mã và kiểm tra Token tự động
        SecretKeySpec secretKeySpec = new SecretKeySpec(SINGER_KEY.getBytes(), "HS256");

        // Tạo bộ giải mã gốc của Nimbus
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // Tùy biến lại phương thức decode để kiểm tra danh sách đen

        return token -> {
            try {
                // Sử dụng bộ giải mã gốc để kiểm tra cấu trúc, chữ ký và thời gian hết hạn trước
                Jwt jwt = nimbusJwtDecoder.decode(token);

                // Lấy ra chuỗi JTI (JWT ID) từ Token
                String jti = jwt.getId();

                // Nếu JTI này tồn tại trong bảng InvalidatedToken -> Ném lỗi từ chối ngay lập tức
                if (invalidatedTokenRepository.existsById(jti)){
                    throw new JwtException("Token has been invalidated");
                }

                return jwt;
            }catch (Exception e){
                throw new JwtException(e.getMessage());
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // ĐỂ TRỐNG PREFIX: Vì chúng ta đã chủ động phân rã "ROLE_" trong buildScope
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
