package com.example.demo.service.impl;

import com.example.demo.entity.UserMng;
import com.example.demo.repository.UserManagerRepository;
import com.example.demo.service.UserManagerService;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService, UserManagerService {

  private final UserManagerRepository userManagerRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username){
    log.info("Loading user by username: {}", username);
    return userManagerRepository.findByUsername(username).orElse(null);
  }



  @Override
  public UserMng createUser(String username, String password) {
    log.info("Creating user with username: {}", username);
    if(userManagerRepository.findByUsername(username).isPresent()){
      log.error("User with username {} already exists", username);
      throw new RuntimeException("User already exists");
    }
    UserMng user = new UserMng();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    user.setCreatedBy("System");
    user.setUpdatedBy("System");
    user.setCreatedDate(LocalDateTime.now());
    user.setUpdatedDate(LocalDateTime.now());
    userManagerRepository.save(user);
    log.info("Creating Success user with id: {}", user.getId());
    return user;
  }

  @Override
  public UserMng findByUsernameAndPassword(String username, String password) {
    try {
      Optional<UserMng> userMng = userManagerRepository.findByUsername(username);
      if (userMng.isPresent()) {
        UserMng user = userMng.get();
        if (passwordEncoder.matches(password, user.getPassword())) {
          log.info("User found with username: {}", username);
          return user;
        }
      }
    } catch (Exception e) {
      log.info("Error while finding user with username: {}", username);
    }
    return null;
  }
}
