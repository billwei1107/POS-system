package com.enterprise.auth.controller;

import com.enterprise.auth.dto.RegisterRequest;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.service.UserService;
import com.enterprise.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<User> createUser(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserById(id));
    }
}
