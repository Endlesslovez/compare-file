package com.example.demo.config;

import com.example.demo.entity.HistoryLogin;
import com.example.demo.service.HistoryLoginService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Configuration
@RequiredArgsConstructor
public class DatabaseUserFilter extends OncePerRequestFilter {

  private final HistoryLoginService historyLoginService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    if(request.getRequestURI().contains("/create-user") || request.getRequestURI().contains("/create-new-user")) {
      filterChain.doFilter(request, response);
      return;
    }
    String sessionId = request.getSession().getId();
    log.info("Session ID: {}", sessionId);
    HistoryLogin historyLogin = historyLoginService.findBySessionId(sessionId);

    if (historyLogin == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      SecurityContextHolder.getContext().setAuthentication(
          new UsernamePasswordAuthenticationToken(historyLogin.getUsername(),
              historyLogin.getUsername(), null)
      );
    } catch (UsernameNotFoundException e) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
    }
    filterChain.doFilter(request, response);
  }
}
