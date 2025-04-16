package com.example.demo.repository;

import com.example.demo.entity.HistoryLogin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryLoginRepository extends JpaRepository<HistoryLogin, Long> {
    // Custom query methods can be defined here if needed

  Optional<HistoryLogin> findByUsername(String username);

  Optional<HistoryLogin> findBySessionId(String sessionId);
}
