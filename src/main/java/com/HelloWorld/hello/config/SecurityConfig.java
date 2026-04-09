package com.HelloWorld.hello.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // <--- Kích hoạt bảo mật tầng Method (@PreAuthorize)
public class SecurityConfig {
    @Value("${jwt.signerKey}")
    protected String SINGER_KEY;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
//        http
//                .csrf(csfr -> csfr.disable()) // Tạm thời tắt CSRF để có thể POST/DELETE trên Postman
//                .authorizeHttpRequests(auth ->auth
//                        .requestMatchers("/auth/**").permitAll()
//                        .requestMatchers(HttpMethod.GET,"/api/users/**").permitAll() // ✅ Cho phép xem User tự do
//                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN") // Chỉ ADMIN mới được xóa
//                        .anyRequest().authenticated() // Các lệnh khác (POST, PUT) chỉ cần Login là được
//                )
//                .httpBasic(Customizer.withDefaults());

        //Lên đời bằng cấu hình JWT.
        http.authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.POST, "/api/users").permitAll() //Bạn hãy tạm thời thêm để có thể tạo được user đầu tiên mà không bị chặn (401/403).
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/introspect").permitAll()
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
        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Chuyển "ADMIN" trong Token thành "ROLE_ADMIN" trong hệ thống Spring
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

//    public InMemoryUserDetailsManager userDetailsSerivce(){
//        // tạo User quyền ADMIN
//        UserDetails admin = User.withDefaultPasswordEncoder()
//                .username("admin")
//                .password("123456")
//                .roles("ADMIN")
//                .build();
//
//        // Tạo User thường
//        UserDetails user = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("123456")
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(admin,user);
//    }
}
