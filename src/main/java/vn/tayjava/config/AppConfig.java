package vn.tayjava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//Class AppConfig chứa các cấu hình và các Bean cần được Spring quản lý.
public class AppConfig {

    // khởi tạo Spring Security (cho swagger)

    // Config Spring Web configuer (cho API)

    // Khoi tạo Bean cho passwordEncoder

    /*
     * Cấu hình Spring Security cho các API:
     * - Tắt CSRF
     * - Tạm thời cho phép truy cập tất cả API
     * - Không sử dụng session
     */

    @Bean
    //Hãy gọi phương thức này, lấy object trả về và đưa object đó vào Spring Container.
    //Nếu request không vượt qua Spring Security thì nó sẽ không chạy vào Controller.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //Mỗi request từ frontend hoặc Postman gửi vào backend sẽ đi qua chuỗi bộ lọc này trước khi đến Controller

        //Tắt chức năng bảo vệ CSRF của Spring Security
        http.csrf(AbstractHttpConfigurer::disable)
                //Đường dẫn nào được truy cập tự do và đường dẫn nào bắt buộc đăng nhập.
                .authorizeHttpRequests(request -> request

                        // /** có nghĩa là toàn bộ đường dẫn trong ứng dụng Cho phép mọi người truy cập, không yêu cầu đăng nhập và không cần token.
                        .requestMatchers("/**").permitAll()

                        //Những request còn lại bắt buộc phải đăng nhập.
                        .anyRequest().authenticated())

                //Backend không tạo session và không lưu trạng thái đăng nhập của người dùng trên server.
                // 1. Người dùng gửi username/password.
                // 2. Server tạo session.
                // 3. Server lưu thông tin đăng nhập.
                // 4. Trình duyệt giữ session ID trong cookie.
                // 5. Những request sau gửi session ID lên server.
                .sessionManagement(manager ->
                        //server không lưu trạng thái đăng nhập
                        //Mỗi request phải tự mang thông tin xác thực, thường là JWT:
                        manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    //Không cần kiểm tra bảo mật đối với một số đường dẫn nhất định.
    // ignoring thì Request không đi qua Spring Security Filter Chain. Còn permitAll vẫn đi qua
    // Thông thường ignoring() thích hợp hơn với tài nguyên tĩnh
    // Còn các API thường nên dùng: permitAll
    public WebSecurityCustomizer ignoreResources() {
        return webSecurity -> webSecurity
                .ignoring()
                .requestMatchers(
                        //Actuator cung cấp các endpoint kiểm tra trạng thái ứng dụng
                        "/actuator/**",
                        "/v3/**",
                        "/webjars/**",
                        "/swagger-ui/*swagger-initializer.js",
                        "/swagger-ui/**"
                );
        //Các đường dẫn này sẽ được Spring Security bỏ qua hoàn toàn.
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
