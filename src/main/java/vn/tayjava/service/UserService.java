package vn.tayjava.service;

import vn.tayjava.controller.request.UserCreationRequest;
import vn.tayjava.controller.request.UserPasswordRequest;
import vn.tayjava.controller.request.UserUpdateRequest;
import vn.tayjava.controller.response.UserPageResponse;
import vn.tayjava.controller.response.UserResponse;

import java.util.List;

public interface UserService {
    UserPageResponse findAll(String keyword, String sortBy, int page, int pageSize);

    UserResponse findById(Long id);

    UserResponse findByUsername(String username);

    UserResponse findByEmail(String email);

    Long save(UserCreationRequest req);

    void update(UserUpdateRequest req);

    void changePassword(UserPasswordRequest req);

    void delete(Long id);
}
