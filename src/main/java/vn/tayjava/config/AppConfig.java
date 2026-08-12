package vn.tayjava.config;

import com.sendgrid.SendGrid;
import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.tayjava.service.UserServiceDetail;

@Configuration
@RequiredArgsConstructor
// Class chứa các cấu hình Spring Security, CORS, mã hóa mật khẩu và SendGrid.
public class AppConfig {

    // Lấy SendGrid API Key từ application.yml hoặc application.properties.
    @Value("${spring.sendgrid.api-key}")
    private String sendgridApiKey;

    // Filter tự tạo dùng để kiểm tra JWT trong mỗi request.
    private final CustomizeRequestFilter requestFilter;

    // Service dùng để lấy thông tin người dùng từ database.
    private final UserServiceDetail userServiceDetail;

    /**
     * Cấu hình Spring Security.
     *
     * Request sẽ đi qua Security Filter Chain trước khi vào Controller.
     * Nếu JWT không hợp lệ hoặc chưa đăng nhập thì request sẽ bị chặn.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Tắt CSRF vì ứng dụng sử dụng JWT và không dùng session.
                .csrf(AbstractHttpConfigurer::disable)

                // Cấu hình quyền truy cập cho các đường dẫn.
                .authorizeHttpRequests(request -> request

                        // Các API đăng nhập, đăng ký... được truy cập không cần JWT.
                        .requestMatchers("/auth/**").permitAll()

                        // Các API còn lại bắt buộc phải đăng nhập.
                        .anyRequest().authenticated()
                )

                // Server không lưu trạng thái đăng nhập bằng session.
                // Mỗi request phải tự gửi JWT lên để xác thực.
                .sessionManagement(manager ->
                        manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Sử dụng AuthenticationProvider do chúng ta cấu hình bên dưới.
                .authenticationProvider(authenticationProvider())

                // Cho CustomizeRequestFilter chạy trước filter đăng nhập mặc định.
                .addFilterBefore(
                        requestFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Các đường dẫn bên dưới được bỏ qua hoàn toàn bởi Spring Security.
     *
     * ignoring(): không đi qua Security Filter Chain.
     * permitAll(): vẫn đi qua Filter Chain nhưng không cần đăng nhập.
     */
    @Bean
    public WebSecurityCustomizer ignoreResources() {
        return webSecurity -> webSecurity
                .ignoring()
                .requestMatchers(
                        // Endpoint theo dõi trạng thái ứng dụng.
                        "/actuator/**",

                        // Các đường dẫn phục vụ Swagger/OpenAPI.
                        "/v3/**",
                        "/webjars/**",
                        "/swagger-ui/*swagger-initializer.js",
                        "/swagger-ui/**"
                );
    }

    /**
     * Cấu hình CORS để frontend chạy tại localhost:8500
     * có thể gọi API của backend.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry
                        // Áp dụng CORS cho toàn bộ API.
                        .addMapping("/**")

                        // Chỉ cho phép frontend này gọi backend.
                        .allowedOrigins("http://localhost:8500")

                        // Các phương thức HTTP được phép sử dụng.
                        .allowedMethods("GET", "POST", "PUT", "DELETE")

                        // Cho phép gửi tất cả loại header.
                        .allowedHeaders("*")

                        // Không cho phép gửi thông tin xác thực bằng cookie.
                        .allowCredentials(false)

                        // Trình duyệt lưu kết quả kiểm tra CORS trong 3600 giây.
                        .maxAge(3600);
            }
        };
    }

    /**
     * AuthenticationManager dùng để xử lý quá trình đăng nhập.
     * Ví dụ: kiểm tra username và password.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * DaoAuthenticationProvider thực hiện:
     * - Lấy người dùng từ database.
     * - So sánh mật khẩu.
     * - Xác thực thông tin đăng nhập.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider();

        // Dùng BCrypt để kiểm tra mật khẩu.
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        // Dùng UserServiceDetail để lấy thông tin người dùng.
        authenticationProvider.setUserDetailsService(
                userServiceDetail.userDetailsService()
        );

        return authenticationProvider;
    }

    // Bean dùng để mã hóa và so sánh mật khẩu bằng BCrypt.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Tạo đối tượng SendGrid để sử dụng chức năng gửi email.
    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendgridApiKey);
    }
}