package com.example.ProjectManagement.model.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;

@Entity
@Table(name = "role_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_role_permission",
                        columnNames = {"role_id", "permission_id"}
                )},
indexes = {@Index(name = "idx_role_id", columnList = "role_id"),
        @Index(name = "idx_permission_id", columnList = "permission_id")
})
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
