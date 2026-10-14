package com.cinewave.repository;

import com.cinewave.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cinewave.entity.Role;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    long count();

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE " +
           ":query IS NULL OR :query = '' OR " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@org.springframework.data.repository.query.Param("query") String query);
}
