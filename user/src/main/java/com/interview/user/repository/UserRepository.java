package com.interview.user.repository;

import com.interview.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(UUID id);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByKeycloakSubject(String keycloakSubject);

    @Query("""
            select u from User u
            where :keyword is null
               or lower(u.username) like lower(concat('%', :keyword, '%'))
               or lower(u.email) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(u.displayName, '')) like lower(concat('%', :keyword, '%'))
            """)
    Page<User> search(String keyword, Pageable pageable);
}
