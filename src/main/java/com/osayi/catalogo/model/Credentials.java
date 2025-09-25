// src/main/java/com/osayi/catalogo/model/Credentials.java
package com.osayi.catalogo.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Credentials {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username; // email
  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  private Role role = Role.USER;

  public enum Role { USER, ADMIN }

  @OneToOne
@JoinColumn(name = "app_user_id")
private AppUser appUser;

// Getter/Setter usati dai controller via reflection
public AppUser getAppUser() { return this.appUser; }
public void setAppUser(AppUser u) { this.appUser = u; }

}
