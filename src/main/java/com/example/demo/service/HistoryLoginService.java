package com.example.demo.service;

import com.example.demo.entity.HistoryLogin;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface HistoryLoginService {
    /**
     * Saves a history login record.
     *
     * @param username the username of the user
     */
    void saveHistoryLogin(String username, HttpServletRequest request, boolean isLogout);

    /**
     * Retrieves a history login record by username.
     *
     * @param username the username of the user
     * @return an Optional containing the HistoryLogin object if found, or empty if not found
     */
    Optional<HistoryLogin> getHistoryLoginByUsername(String username);


    boolean validateLoginFirstTime(String username);

    boolean validateLoginFirstTime(HttpServletRequest request);

    HistoryLogin findBySessionId(String sessionId);

}
