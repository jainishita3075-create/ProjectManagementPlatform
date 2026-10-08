package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.Request.ChangePasswordRequest;
import com.example.ProjectManagement.DTO.Request.LoginRequest;
import com.example.ProjectManagement.DTO.Request.RegisterRequest;
import com.example.ProjectManagement.DTO.Request.UpdateUserRoleRequest;
import com.example.ProjectManagement.DTO.Response.ApiResponse;
import com.example.ProjectManagement.DTO.Response.UserResponse;
import com.example.ProjectManagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService service;

//    @GetMapping("/hello")
//    public String hello(){
//        return "JWT authentication is working perfectly!";
//    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(
            @RequestBody RegisterRequest request){
        UserResponse user = service.register(request);
        return ApiResponse.success("User registered successfully", user);
    }

    @GetMapping("/api/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = service.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest request) {
        String token = service.verify(request);
        if ("fail".equals(token)) {
            return ApiResponse.error("Invalid username or password");
        }
        return ApiResponse.success("Login successful", token);
    }

    @GetMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication) {
        UserResponse response = service.getProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", response));
    }

    @PatchMapping("/api/users/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestBody java.util.Map<String, String> body,
            Authentication authentication) {
        String newName = body.get("name");
        UserResponse response = service.updateProfile(newName, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }


    @PostMapping("/api/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully. Please clear your token.", null));
    }

    @PatchMapping("/api/users/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        service.changePassword(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @PatchMapping("/api/admin/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request,
            Authentication authentication) {
        UserResponse response = service.updateUserRole(userId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", response));
    }

}
