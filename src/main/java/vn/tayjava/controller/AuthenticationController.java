package vn.tayjava.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tayjava.controller.request.SignInRequest;
import vn.tayjava.controller.response.TokenResponse;
import vn.tayjava.service.AuthenticationService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
@Tag(name = "Authentication-Controller")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Access Token", description = "Get access token and refresh token by username and password")
    @PostMapping("/access-token")
    public TokenResponse getAccessToken(@RequestBody SignInRequest request) {
        log.info("Access Token Request");
//        return TokenResponse.builder()
//                .accessToken("DUMMY-ACCESS_TOKEN")
//                .refreshToken("DUMMY-REFRESH_TOKEN")
//                .build();

        return authenticationService.getAccessToken(request);
    }

    @Operation(summary = "Refresh Token", description = "Get new access token by refresh token")
    @PostMapping("/refresh-token")
    public TokenResponse getRefreshToken(@RequestBody String refreshToken) {
        log.info("Refresh Token Request");
//        return TokenResponse.builder()
//                .accessToken("DUMMY-NEW-ACCESS_TOKEN")
//                .refreshToken("DUMMY-REFRESH_TOKEN")
//                .build();

        return authenticationService.getRefreshToken(refreshToken);
    }
}
