package com.interview.user.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class User {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "keycloak_subject", nullable = false, unique = true, updatable = false)
    private String keycloakSubject;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 30)
    private UserRole role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected User() {
    }

    public User(String keycloakSubject, String username, String email,
                String displayName, UserRole role, boolean enabled) {
        this.keycloakSubject = keycloakSubject;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
        this.enabled = enabled;
    }

    @PrePersist
    void create() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public void update(String username, String email, String displayName, UserRole role, boolean enabled) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
        this.enabled = enabled;
    }

    public UUID getId() { return id; }
    public String getKeycloakSubject() { return keycloakSubject; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public UserRole getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
