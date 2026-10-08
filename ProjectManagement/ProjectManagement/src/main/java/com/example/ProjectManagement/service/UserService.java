package com.example.ProjectManagement.service;

import com.example.ProjectManagement.DTO.Request.ChangePasswordRequest;
import com.example.ProjectManagement.DTO.Request.LoginRequest;
import com.example.ProjectManagement.DTO.Request.RegisterRequest;
import com.example.ProjectManagement.DTO.Request.UpdateUserRoleRequest;
import com.example.ProjectManagement.DTO.Response.UserResponse;
import com.example.ProjectManagement.exception.BadRequestException;
import com.example.ProjectManagement.exception.ResourceNotFoundException;
import com.example.ProjectManagement.model.Entity.Roles;
import com.example.ProjectManagement.model.Entity.Users;
import com.example.ProjectManagement.model.Enum.UserRole;
import com.example.ProjectManagement.repository.RolesRepo;
import com.example.ProjectManagement.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RolesRepo rolesRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse register(RegisterRequest request){
        if (repo.findByUserName(request.getUsername()) != null) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        Users user = new Users();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setUserName(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setEmailVerified(false);

        Roles memberRole = rolesRepo.findByRole(UserRole.MEMBER);
        if (memberRole == null) {
            memberRole = new Roles();
            memberRole.setRole(UserRole.MEMBER);
            memberRole.setDescription("Default member role");
            memberRole = rolesRepo.save(memberRole);
        }
        Set<Roles> roles = new HashSet<>();
        roles.add(memberRole);
        user.setRoles(roles);
        Users saved = repo.save(user);
        return mapToResponse(saved);
    }

    public UserResponse getProfile(String username) {
        Users user = repo.findByUserName(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        return mapToResponse(user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse updateProfile(String name, String username) {
        Users user = repo.findByUserName(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        Users saved = repo.save(user);
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, String adminUsername) {
        Users targetUser = repo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        if (targetUser.getUserName().equals(adminUsername) && request.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("Admins cannot demote their own account.");
        }
        Roles newRole = rolesRepo.findByRole(request.getRole());
        if (newRole == null) {
            newRole = new Roles();
            newRole.setRole(request.getRole());
            newRole.setDescription(request.getRole().name() + " role");
            newRole = rolesRepo.save(newRole);
        }
        Set<Roles> roles = new HashSet<>();
        roles.add(newRole);
        targetUser.setRoles(roles);
        Users updated = repo.save(targetUser);
        return mapToResponse(updated);
    }

    public String verify(LoginRequest request) {
        Users user = repo.findByUserName(request.getUsername());
        if (user == null) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return jwtService.generateToken(request.getUsername());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String username) {
        Users user = repo.findByUserName(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + username);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Incorrect current password");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password cannot be the same as the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        repo.save(user);
    }

    @Cacheable(value = "users")
    public List<UserResponse> getAllUsers() {
        return repo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    private UserResponse mapToResponse(Users user){
        Set<UserRole> roleEnums = user.getRoles() != null ?
                user.getRoles()
                        .stream()
                        .map(Roles::getRole)
                        .collect(Collectors.toSet()) : Set.of();

        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUserName())
                .active(user.isActive())
                .roles(roleEnums)
                .build();
    }
}
