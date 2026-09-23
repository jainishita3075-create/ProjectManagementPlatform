package com.example.ProjectManagement.model.Entity;

import com.example.ProjectManagement.model.Enum.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@Table(name = "user_session",
indexes = {@Index(name = "idx_usess_user_id", columnList = "user_id"),
        @Index(name = "idx_refresh_token_hash", columnList = "refresh_token_hash", unique = true),
        @Index(name = "idx_session_status", columnList = "session_status"),
        @Index(name = "idx_usess_expires_at", columnList = "expires_at")
})
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userSessionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users userId;

    @Column(name = "login_time", nullable = false)
    private Instant loginTime;

    @Column(name = "logout_time")
    private Instant logoutTime;

    @Column(name = "ip_add", nullable = false)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status")
    private SessionStatus sessionStatus;

    @Column(name = "refresh_token_hash", nullable = false, unique = true)
    private String refreshHashToken;

    @Column(name = "last_used")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}
