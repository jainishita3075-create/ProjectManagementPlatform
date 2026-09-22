package com.example.ProjectManagement.model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;

@Entity
@Table(name = "role_permissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolePermissions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long rolePermissionId;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Roles roleId;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permissions permissionId;
}
