package vn.tayjava.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import vn.tayjava.common.TokenType;
import vn.tayjava.exception.InvalidDataException;
import vn.tayjava.service.JwtService;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j(topic = "JWT-SERVICE")
public class JwtServiceImpl implements JwtService {

    // Thời gian tồn tại của Access Token, tính theo phút
    @Value("${jwt.expiryMinutes}")
    private Long expiryMinutes;

    // Thời gian tồn tại của Refresh Token, tính theo ngày
    @Value("${jwt.expiryDay}")
    private Long expiryDay;

    // Secret Key dùng để tạo và xác thực Access Token
    @Value("${jwt.accessKey}")
    private String accessKey;

    // Secret Key dùng để tạo và xác thực Refresh Token
    @Value("${jwt.refreshKey}")
    private String refreshKey;

    // Tạo Access Token sau khi user đăng nhập thành công
    @Override
    public String generateAccessToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        log.info("Generate access token for user {} with authorities {}", userId, authorities);

        // Claims là các thông tin bổ sung được lưu bên trong payload của JWT
        Map<String, Object> claims = new HashMap<String, Object>();

        // Lưu id của user vào Token
        claims.put("userId", userId);

        // Lưu role/quyền của user vào Token
        claims.put("role", authorities);

        // Gọi hàm bên dưới để tạo Access Token hoàn chỉnh
        return generateToken(claims, username);
    }

    // Tạo Refresh Token, dùng để xin Access Token mới khi Access Token hết hạn
    @Override
    public String generateRefreshToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        log.info("Generate refresh token for user {} with authorities {}", userId, authorities);

        // Tạo các dữ liệu muốn lưu trong Refresh Token
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("userId", userId);
        claims.put("role", authorities);

        // Gọi hàm tạo Refresh Token
        return generateRefreshToken(claims, username);
    }

    // Lấy username từ Token
    @Override
    public String extractUsername(String token, TokenType type) {
        log.info("Extract username from token {} with type {}", token, type);

        // Username được lưu trong subject (sub) của JWT
        return extractClaims(type, token, Claims::getSubject);
    }

    // Hàm dùng chung để lấy một thông tin cụ thể từ Claims
    private <T> T extractClaims(TokenType tokenType, String token, Function<Claims, T> claimsExtractor) {

        // Parse Token để lấy toàn bộ Claims bên trong
        final Claims claims = extraAllClaim(token, tokenType);

        // Lấy dữ liệu cần thiết, ví dụ username, ngày hết hạn,...
        return claimsExtractor.apply(claims);
    }

    // Giải mã/parse Token để lấy toàn bộ Claims
    private Claims extraAllClaim(String token, TokenType tokenType) {
        try {

            // Dùng secret key để kiểm tra chữ ký Token
            // Nếu Token hợp lệ thì trả về phần payload (Claims)
            return Jwts.parser().setSigningKey(accessKey).parseClaimsJws(token).getBody();

        } catch (SignatureException | ExpiredJwtException e) {

            // SignatureException: Token sai chữ ký hoặc đã bị chỉnh sửa
            // ExpiredJwtException: Token đã hết hạn
            throw new AccessDeniedException("Access Denied!, error: " + e.getMessage());
        }
    }

    // Hàm tạo Access Token
    private String generateToken(Map<String, Object> claims, String username){
        log.info("Generate token for user {} with authorities {}", username, claims);

        return Jwts.builder()

                // Đưa các dữ liệu như userId, role vào payload
                .setClaims(claims)

                // Lưu username vào trường subject (sub)
                .setSubject(username)

                // Thời điểm Token được tạo
                .setIssuedAt(new Date())

                // Thời điểm Token hết hạn = thời gian hiện tại + expiryMinutes
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * expiryMinutes))

                // Ký Token bằng accessKey và thuật toán HS256
                .signWith(getKey(TokenType.ACCESS_TOKEN), SignatureAlgorithm.HS256)

                // Ghép header + payload + signature thành JWT hoàn chỉnh
                .compact();
    }

    // Hàm tạo Refresh Token
    private String generateRefreshToken(Map<String, Object> claims, String username){
        log.info("----------[ generateRefreshToken ]----------");

        return Jwts.builder()

                // Lưu các thông tin bổ sung như userId, role
                .setClaims(claims)

                // Lưu username vào subject
                .setSubject(username)

                // Thời điểm tạo Refresh Token
                .setIssuedAt(new Date())

                // Refresh Token có thời gian sống tính theo ngày
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * expiryDay))

                // Refresh Token dùng refreshKey riêng để ký
                .signWith(getKey(TokenType.REFRESH_TOKEN), SignatureAlgorithm.HS256)

                // Tạo chuỗi JWT hoàn chỉnh
                .compact();
    }

    // Lấy Secret Key tương ứng với từng loại Token
    private Key getKey(TokenType type ) {
        switch (type) {
            case ACCESS_TOKEN -> {

                // accessKey đang ở dạng Base64 nên cần decode trước khi tạo Key
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
            }
            case REFRESH_TOKEN -> {

                // Refresh Token sử dụng refreshKey riêng
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            }
            default -> throw new IllegalArgumentException("Invalid token type");
        }
    }
}