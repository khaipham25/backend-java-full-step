package vn.tayjava.service;

import vn.tayjava.common.TokenType;
import vn.tayjava.controller.request.SignInRequest;
import vn.tayjava.controller.response.TokenResponse;

public interface AuthenticationService {
    TokenResponse getAccessToken(SignInRequest signInRequest);

    TokenResponse getRefreshToken(String request);

}
