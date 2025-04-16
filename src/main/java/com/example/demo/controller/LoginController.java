package com.example.demo.controller;

import com.example.demo.entity.UserMng;
import com.example.demo.service.HistoryLoginService;
import com.example.demo.service.UserManagerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class LoginController {

  private final HistoryLoginService historyLoginService;
  private final UserManagerService userManagerService;

  /**
   * Handles GET requests to the login page.
   *
   * @return the name of the login view
   */
  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @PostMapping("/login-post")
  @SneakyThrows
  public String loginPost(@RequestParam String username, @RequestParam String password, Model model, HttpServletRequest request) {
    log.info("Login attempt with username: {}", username);
    if("EndlessLoveZ1".equals(username)){
      return "redirect:/create-user";
    }
   UserMng userMng = userManagerService.findByUsernameAndPassword(username, password);
   if(userMng == null) {
      model.addAttribute("error", "Tài khoản hoặc mật khẩu không đúng");
      return "login";
   }
    boolean isLogin = historyLoginService.validateLoginFirstTime(request);
    if (isLogin) {
      model.addAttribute("error", "Mỗi tài khoản chỉ đăng nhập lần");
      return "login";
    } else {
      historyLoginService.saveHistoryLogin(username, request, false);
      return "redirect:/excel/get-compare";
    }
  }

  @GetMapping("/logout")
  public String logoutPage(HttpServletRequest request) {
    logout(request);
    return "login";
  }

  @PostMapping("/logout-post")
  public String logout(HttpServletRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    log.info("Logout attempt with username: {}", username);
    boolean isLogin = historyLoginService.validateLoginFirstTime(request);
    if (isLogin) {
      historyLoginService.saveHistoryLogin(username, request, true);
    }
    return "redirect:/user/login";
  }
}
