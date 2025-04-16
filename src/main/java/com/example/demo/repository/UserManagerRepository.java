package com.example.demo.repository;

import com.example.demo.entity.UserMng;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserManagerRepository extends JpaRepository<UserMng, Long> {
    /**
     * Finds a user by their username.
     *
     * @param username the username of the user
     * @return the user with the specified username, or null if not found
     */
    Optional<UserMng> findByUsername(String username);

    Optional<UserMng> findByUsernameAndPassword(String username, String password);

}
