package com.example.demo.controller;

import com.example.demo.entity.UserMng;
import com.example.demo.service.UserManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {
  private final UserManagerService userManagerService;

  @GetMapping("/create-user")
  public String showCreateUserForm() {
    return "create_user";
  }

  @RequestMapping(value = "/create-new-user", method = {RequestMethod.POST})
  public String createUserApi(@RequestParam String username, @RequestParam String password, Model model) {
    try {
      UserMng user = userManagerService.createUser(username, password);
      model.addAttribute("Result", "Created user: " + user.getUsername() + "success!!!");
    }catch (Exception e){
      log.info("Error creating user: {}", e.getMessage());
      model.addAttribute("Result", "Error creating user: " + e.getMessage());
    }
    return "create_user";
  }
}