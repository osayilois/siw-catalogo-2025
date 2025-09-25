// src/main/java/com/osayi/catalogo/security/AuthSuccessHandler.java
package com.osayi.catalogo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

  private final RequestCache requestCache = new HttpSessionRequestCache();

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication)
      throws IOException, ServletException {

    boolean isAdmin = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch("ROLE_ADMIN"::equals);

    String targetUrl;
    if (isAdmin) {
      targetUrl = "/admin/dashboard";          // admin → dashboard
    } else {
      // utente normale → torna dove voleva andare, altrimenti home
      SavedRequest saved = requestCache.getRequest(request, response);
      targetUrl = (saved != null) ? saved.getRedirectUrl() : "/";
    }

    // pulizia eventuali errori precedenti di auth
    var session = request.getSession(false);
    if (session != null) {
      session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
    }

    response.sendRedirect(targetUrl);
  }
}
