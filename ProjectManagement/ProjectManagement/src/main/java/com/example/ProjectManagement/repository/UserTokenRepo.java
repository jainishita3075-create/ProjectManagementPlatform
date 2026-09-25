package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTokenRepo extends JpaRepository<UserToken, Long> {
    UserToken findByUserTokenId(Long userTokenId);
}
