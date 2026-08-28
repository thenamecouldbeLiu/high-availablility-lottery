package com.interview.user.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
@Getter
@Setter
public class User {
    @Getter
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Getter
    @Column(name = "keycloak_subject", nullable = false, unique = true, updatable = false)
    private String keycloakSubject;

    @Getter
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "display_name", length = 150)
    private String displayName;

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
                String displayName, boolean enabled) {
        this.keycloakSubject = keycloakSubject;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
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

    public void update(String username, String email, String displayName, boolean enabled) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.enabled = enabled;
    }
}
