package com.example.ProjectManagement.Config;

import com.example.ProjectManagement.model.Entity.*;
import com.example.ProjectManagement.model.Enum.*;
import com.example.ProjectManagement.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Value("${app.seed.password:Password123#}")
    private String defaultPassword;

    @Bean
    public ApplicationRunner seedDatabase(
            UserRepo userRepo,
            RolesRepo rolesRepo,
            ProjectRepo projectRepo,
            ProjectMembersRepo projectMembersRepo,
            TaskRepo taskRepo,
            TaskAssignementRepo taskAssignmentRepo,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            for (UserRole roleEnum : UserRole.values()) {
                if (rolesRepo.findByRole(roleEnum) == null) {
                    Roles r = new Roles();
                    r.setRole(roleEnum);
                    r.setDescription(roleEnum.name() + " role");
                    rolesRepo.save(r);
                }
            }

            Roles adminRole = rolesRepo.findByRole(UserRole.ADMIN);
            Roles managerRole = rolesRepo.findByRole(UserRole.MANAGER);
            Roles memberRole = rolesRepo.findByRole(UserRole.MEMBER);

            Users admin = userRepo.findByUserName("admin");
            if (admin == null) {
                admin = new Users();
                admin.setName("Super Admin");
                admin.setEmail("admin@projectmngmnt.com");
                admin.setUserName("admin");
                admin.setPasswordHash(passwordEncoder.encode(defaultPassword));
                admin.setActive(true);
                admin.setEmailVerified(true);
                admin.setRoles(Set.of(adminRole));
                admin = userRepo.save(admin);
                System.out.println(">>> DataSeeder: Created Admin user (admin / " + defaultPassword + ")");
            }

            Users ayan = userRepo.findByUserName("ayan_verma");
            if (ayan == null) {
                // Also check if legacy john_doe exists to migrate
                Users legacy = userRepo.findByUserName("john_doe");
                if (legacy != null) {
                    legacy.setName("Ayan Verma");
                    legacy.setEmail("ayan@example.com");
                    legacy.setUserName("ayan_verma");
                    ayan = userRepo.save(legacy);
                } else {
                    ayan = new Users();
                    ayan.setName("Ayan Verma");
                    ayan.setEmail("ayan@example.com");
                    ayan.setUserName("ayan_verma");
                    ayan.setPasswordHash(passwordEncoder.encode(defaultPassword));
                    ayan.setActive(true);
                    ayan.setEmailVerified(true);
                    ayan.setRoles(Set.of(managerRole));
                    ayan = userRepo.save(ayan);
                }
            }

            Users rhea = userRepo.findByUserName("rhea_gupta");
            if (rhea == null) {
                // Also check if legacy sarah_connor exists to migrate
                Users legacy = userRepo.findByUserName("sarah_connor");
                if (legacy != null) {
                    legacy.setName("Rhea Gupta");
                    legacy.setEmail("rhea@example.com");
                    legacy.setUserName("rhea_gupta");
                    rhea = userRepo.save(legacy);
                } else {
                    rhea = new Users();
                    rhea.setName("Rhea Gupta");
                    rhea.setEmail("rhea@example.com");
                    rhea.setUserName("rhea_gupta");
                    rhea.setPasswordHash(passwordEncoder.encode(defaultPassword));
                    rhea.setActive(true);
                    rhea.setEmailVerified(true);
                    rhea.setRoles(Set.of(memberRole));
                    rhea = userRepo.save(rhea);
                }
            }

            Users tanvi = userRepo.findByUserName("tanvi_sharma");
            if (tanvi == null) {
                // Also check if legacy alex_turner exists to migrate
                Users legacy = userRepo.findByUserName("alex_turner");
                if (legacy != null) {
                    legacy.setName("Tanvi Sharma");
                    legacy.setEmail("tanvi@example.com");
                    legacy.setUserName("tanvi_sharma");
                    tanvi = userRepo.save(legacy);
                } else {
                    tanvi = new Users();
                    tanvi.setName("Tanvi Sharma");
                    tanvi.setEmail("tanvi@example.com");
                    tanvi.setUserName("tanvi_sharma");
                    tanvi.setPasswordHash(passwordEncoder.encode(defaultPassword));
                    tanvi.setActive(true);
                    tanvi.setEmailVerified(true);
                    tanvi.setRoles(Set.of(memberRole));
                    tanvi = userRepo.save(tanvi);
                }
            }

            Project project = projectRepo.findAll().stream()
                    .filter(p -> "E-Commerce Cloud Migration".equals(p.getName()))
                    .findFirst()
                    .orElse(null);

            if (project == null) {
                project = new Project();
                project.setName("E-Commerce Cloud Migration");
                project.setDescription("Migrate legacy microservices to AWS Kubernetes infrastructure");
                project.setOwner(admin);
                project.setStatus(ProjectStatus.ACTIVE);
                project.setPriority(ProjectPriority.HIGH);
                project.setStartDate(Instant.now().minus(10, ChronoUnit.DAYS));
                project.setDueDate(Instant.now().plus(30, ChronoUnit.DAYS));
                project = projectRepo.save(project);

                ProjectMembers pm1 = new ProjectMembers();
                pm1.setProjId(project);
                pm1.setUserId(rhea);
                pm1.setMemberRole(ProjectMemberRole.EDITOR);
                pm1.setJoinedAt(Instant.now());
                projectMembersRepo.save(pm1);

                ProjectMembers pm2 = new ProjectMembers();
                pm2.setProjId(project);
                pm2.setUserId(tanvi);
                pm2.setMemberRole(ProjectMemberRole.VIEWER);
                pm2.setJoinedAt(Instant.now());
                projectMembersRepo.save(pm2);

                Task t1 = new Task();
                t1.setProjId(project);
                t1.setTitle("Configure Kubernetes Cluster & Ingress");
                t1.setDescription("Set up production EKS cluster with Istio service mesh and ingress routing.");
                t1.setStatus(TaskStatus.IN_PROGRESS);
                t1.setPriority(ProjectPriority.URGENT);
                t1.setCreatedBy(admin);
                t1.setStartDate(Instant.now().minus(2, ChronoUnit.DAYS));
                t1.setDueDate(Instant.now().plus(5, ChronoUnit.DAYS));
                t1.setEstimatedEffortHours(BigDecimal.valueOf(16.0));
                t1 = taskRepo.save(t1);

                TaskAssignment a1 = new TaskAssignment();
                a1.setTaskId(t1);
                a1.setUserId(rhea);
                a1.setAssignedBy(admin);
                a1.setStatus(AssignmentStatus.ACCEPTED);
                a1.setAssignedAt(Instant.now());
                taskAssignmentRepo.save(a1);

                Task t2 = new Task();
                t2.setProjId(project);
                t2.setTitle("Setup PostgreSQL Database Replication");
                t2.setDescription("Provision multi-region read replicas and automated snapshots.");
                t2.setStatus(TaskStatus.COMPLETED);
                t2.setPriority(ProjectPriority.HIGH);
                t2.setCreatedBy(admin);
                t2.setStartDate(Instant.now().minus(7, ChronoUnit.DAYS));
                t2.setDueDate(Instant.now().minus(1, ChronoUnit.DAYS));
                t2.setCompletion_date(Instant.now().minus(1, ChronoUnit.DAYS));
                t2.setEstimatedEffortHours(BigDecimal.valueOf(8.0));
                taskRepo.save(t2);

                Task t3 = new Task();
                t3.setProjId(project);
                t3.setTitle("SSL Certificates Renewal & DNS Mapping");
                t3.setDescription("Certificate renewal deadline passed; urgent action needed.");
                t3.setStatus(TaskStatus.OVERDUE);
                t3.setPriority(ProjectPriority.URGENT);
                t3.setCreatedBy(admin);
                t3.setStartDate(Instant.now().minus(5, ChronoUnit.DAYS));
                t3.setDueDate(Instant.now().minus(2, ChronoUnit.DAYS));
                t3.setEstimatedEffortHours(BigDecimal.valueOf(4.0));
                taskRepo.save(t3);

                Task t4 = new Task();
                t4.setProjId(project);
                t4.setTitle("Payment Gateway Sandbox Integration");
                t4.setDescription("Blocked awaiting vendor API credentials and compliance review.");
                t4.setStatus(TaskStatus.BLOCKED);
                t4.setPriority(ProjectPriority.MEDIUM);
                t4.setCreatedBy(admin);
                t4.setStartDate(Instant.now());
                t4.setDueDate(Instant.now().plus(10, ChronoUnit.DAYS));
                t4.setEstimatedEffortHours(BigDecimal.valueOf(12.0));
                t4 = taskRepo.save(t4);

                TaskAssignment a2 = new TaskAssignment();
                a2.setTaskId(t4);
                a2.setUserId(tanvi);
                a2.setAssignedBy(admin);
                a2.setStatus(AssignmentStatus.ACCEPTED);
                a2.setAssignedAt(Instant.now());
                taskAssignmentRepo.save(a2);

                System.out.println(">>> DataSeeder: Demo projects and tasks seeded successfully!");
            }
        };
    }
}
