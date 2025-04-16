package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "history_login")
@Table(name = "history_login")
@AllArgsConstructor
@NoArgsConstructor
public class HistoryLogin extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="user_name", nullable = false)
  private String username;

  @Column(name="count_login", nullable = false)
  private int countLogin;

  @Column(name="is_login", nullable = false)
  private boolean isLogin;

  @Column(name="session_id")
  private String sessionId;
}
