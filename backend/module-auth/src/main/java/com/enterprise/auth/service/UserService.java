package com.enterprise.auth.service;

import com.enterprise.auth.dto.RegisterRequest;
import com.enterprise.auth.entity.User;
import java.util.UUID;

public interface UserService {
    User createUser(RegisterRequest request);
    User getUserById(UUID id);
    void handleLoginSuccess(User user);
    void handleLoginFailure(User user);
}
