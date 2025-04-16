package com.example.demo.service.impl;

import com.example.demo.entity.HistoryLogin;
import com.example.demo.repository.HistoryLoginRepository;
import com.example.demo.service.HistoryLoginService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class HistoryLoginServiceImpl implements HistoryLoginService {

  private final HistoryLoginRepository repository;

  @Override
  public void saveHistoryLogin(String username, HttpServletRequest request, boolean isLogout) {
    repository.findBySessionId(request.getSession().getId()).ifPresentOrElse(
        historyLogin -> {
          historyLogin.setCountLogin(historyLogin.getCountLogin() + 1);
          historyLogin.setUpdatedDate(LocalDateTime.now());
          historyLogin.setLogin(!historyLogin.isLogin());
          historyLogin.setSessionId(isLogout ? null : request.getSession().getId());
          repository.save(historyLogin);
          log.info("History login updated for user: {}", username);
        },
        () -> {
          HistoryLogin historyLogin = new HistoryLogin();
          historyLogin.setUsername(username);
          historyLogin.setCountLogin(1);
          historyLogin.setLogin(true);
          historyLogin.setCreatedBy("System");
          historyLogin.setUpdatedBy("System");
          historyLogin.setSessionId(request.getSession().getId());
          historyLogin.setCreatedDate(LocalDateTime.now());
          historyLogin.setUpdatedDate(LocalDateTime.now());
          repository.save(historyLogin);
          log.info("History login created for user: {}", username);
        }
    );
  }

  @Override
  public Optional<HistoryLogin> getHistoryLoginByUsername(String username) {
    return repository.findByUsername(username);
  }

  @Override
  public boolean validateLoginFirstTime(String username) {
    Optional<HistoryLogin> historyLoginOptional = repository.findByUsername(username);
    if (historyLoginOptional.isPresent()) {
      HistoryLogin historyLogin = historyLoginOptional.get();
      return historyLogin.isLogin();
    }
    return false;
  }

  @Override
  public boolean validateLoginFirstTime(HttpServletRequest request) {
    Optional<HistoryLogin> historyLoginOptional = repository.findBySessionId(request.getSession().getId());
    if (historyLoginOptional.isPresent()) {
      HistoryLogin historyLogin = historyLoginOptional.get();
      return historyLogin.isLogin();
    }
    return false;
  }

  @Override
  public HistoryLogin findBySessionId(String sessionId) {
    Optional<HistoryLogin> historyLoginOptional = repository.findBySessionId(sessionId);
    return historyLoginOptional.orElse(null);
  }
}
