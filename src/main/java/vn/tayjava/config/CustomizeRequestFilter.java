package vn.tayjava.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.tayjava.common.TokenType;
import vn.tayjava.service.JwtService;
import vn.tayjava.service.UserServiceDetail;

import java.io.IOException;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
@Slf4j(topic = "CUSTOMIZE-REQUEST-FILTER")
@RequiredArgsConstructor
//Code CustomizeRequestFilter này dùng để chặn và kiểm tra JWT của mỗi request trước khi request đi vào Controller.
public class CustomizeRequestFilter extends OncePerRequestFilter {

    // Service dùng để giải mã và kiểm tra JWT
    private final JwtService jwtService;

    // Service dùng để lấy thông tin user từ database
    private final UserServiceDetail userServiceDetail;

    // Hàm này sẽ chạy 1 lần cho mỗi request gửi lên server
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Filtering request {} {}", request.getMethod(), request.getRequestURI());

        // Lấy JWT từ Authorization Header
        // Ví dụ: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        String authorizationHeader = request.getHeader("Authorization");

        // Chỉ xử lý nếu Header tồn tại và bắt đầu bằng "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            // Bỏ chữ "Bearer " để lấy phần JWT phía sau
            authorizationHeader = authorizationHeader.substring(7);
            log.info("Authorization Header: {}", authorizationHeader.substring(0, 20));

            String username = "";

            try {
                // Giải mã Access Token và lấy username bên trong token
                // admin@gmail.com
                username = jwtService.extractUsername(authorizationHeader, TokenType.ACCESS_TOKEN);
                log.info("Username: {}", username);

            } catch (AccessDeniedException e) {

                // Token không hợp lệ, sai chữ ký hoặc hết hạn thì trả lỗi về client
                log.info("Access Denied, message={}",e.getMessage());

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                // Trả response lỗi dưới dạng JSON
                response.getWriter().write(errorResponse(request.getRequestURI(), e.getMessage()));
                return;
            }

            // Dựa vào username lấy thông tin user từ database
            UserDetails userDetails = userServiceDetail.userDetailsService().loadUserByUsername(username);

            // Tạo SecurityContext mới để lưu thông tin user đã đăng nhập
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

            // Tạo đối tượng Authentication chứa user và các quyền của user
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Lưu thêm thông tin của request hiện tại
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Đưa Authentication vào SecurityContext
            securityContext.setAuthentication(authentication);

            // Đưa SecurityContext vào SecurityContextHolder
            // Từ đây Spring Security hiểu request này đã được xác thực
            SecurityContextHolder.setContext(securityContext);
            // == "Thông tin người đang đăng nhập trong request hiện tại"

            // Cho request đi tiếp tới filter tiếp theo hoặc Controller
            filterChain.doFilter(request, response);
            return;

        }

        // Nếu request không có Bearer Token thì vẫn cho đi tiếp
        // Sau đó Spring Security sẽ quyết định API đó có cần đăng nhập hay không
        filterChain.doFilter(request, response);
    }

    /**
     * Tạo nội dung lỗi dạng JSON để trả về client
     * @param message
     * @return
     */
    private String errorResponse(String url, String message) {
        try {
            ErrorResponse error = new ErrorResponse();

            // Thời điểm xảy ra lỗi
            error.setTimestamp(new Date());

            // HTTP status muốn hiển thị trong nội dung JSON
            error.setStatus(HttpServletResponse.SC_FORBIDDEN);

            // API đang được gọi
            error.setPath(url);

            // Tên lỗi
            error.setError("Forbidden");

            // Nội dung chi tiết của lỗi
            error.setMessage(message);

            // Chuyển object ErrorResponse thành chuỗi JSON đẹp, dễ đọc
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(error);

        } catch (Exception e) {
            return ""; // Nếu chuyển sang JSON lỗi thì trả chuỗi rỗng
        }
    }

    // Class dùng để định dạng response khi JWT không hợp lệ
    @Setter
    @Getter
    private class ErrorResponse {
        private Date timestamp;
        private int status;
        private String path;
        private String error;
        private String message;
    }
}