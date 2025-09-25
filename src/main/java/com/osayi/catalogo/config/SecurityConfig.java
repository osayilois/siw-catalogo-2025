package com.osayi.catalogo.config;

import com.osayi.catalogo.repository.CredentialsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService(CredentialsRepository repo) {
    return username -> repo.findByUsername(username)
        .map(c -> User.withUsername(c.getUsername())
            .password(c.getPassword())
            .roles(c.getRole().name())
            .build())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  @Bean
  public DaoAuthenticationProvider authProvider(UserDetailsService uds, PasswordEncoder enc) {
    var p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(enc);
    return p;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(auth -> auth
        // PUBBLICO
        .requestMatchers("/", "/home", "/login", "/register", "/error", "/favicon.ico").permitAll()
        .requestMatchers("/products", "/products/**").permitAll()   // <-- lista + dettagli prodotto
        .requestMatchers("/css/**", "/js/**", "/images/**", "/img/**", "/webjars/**", "/uploads/**").permitAll()

        // SOLO ADMIN
        .requestMatchers("/admin/**").hasRole("ADMIN")

        // SOLO UTENTI AUTENTICATI
        .requestMatchers("/reviews/**").authenticated()             // <-- recensioni protette

        // TUTTO IL RESTO: login richiesto
        .anyRequest().authenticated()
      )

      .formLogin(f -> f
        .loginPage("/login").permitAll()
        .loginProcessingUrl("/login")
        .usernameParameter("username")
        .passwordParameter("password")
        .failureUrl("/login?error")
        // nessun successHandler custom: usiamo quello di default che rispetta la "saved request"
      )

      .logout(l -> l
        .logoutUrl("/logout")
        .logoutSuccessUrl("/")
        .invalidateHttpSession(true)
        .deleteCookies("JSESSIONID")
        .permitAll()
      )

      .csrf(Customizer.withDefaults());

    return http.build();
  }
}
