package vn.tayjava.controller.request;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class SignInRequest implements Serializable {
    private String username;
    private String password;
    // Nền tảng truy cập là j (web, mobile, miniAPP)
    private String platform;
    private String deviceToken;
    private String versionApp;
}
