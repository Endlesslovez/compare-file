package com.example.demo.service.impl;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomAuthenticationToken extends User {

  public CustomAuthenticationToken(String username, String password,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
  }

  public CustomAuthenticationToken(String username, String password, boolean enabled,
      boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
        authorities);
  }
//  private final UserDetails principal;
//
//  public CustomAuthenticationToken(UserDetails principal) {
//    super(principal.getAuthorities());
//    this.principal = principal;
//    setAuthenticated(true); // Mark the token as authenticated
//  }
//
//  @Override
//  public Object getCredentials() {
//    return null; // No credentials are needed in this case
//  }
//
//  @Override
//  public Object getPrincipal() {
//    return principal; // Return the authenticated user
//  }
}
