package fr.eni.bookhub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.eni.bookhub.dto.LoginRequest;
import fr.eni.bookhub.dto.RegisterRequest;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuthServiceTest {

  private static final String PASSWORD = "BookHub!2026a";

  @Autowired
  private AuthService authService;

  @Test
  void registerThenLogin() {
    String email = "auth.service." + System.nanoTime() + "@example.fr";
    authService.register(new RegisterRequest("Jean", "Test", email, PASSWORD, true));
    var login = authService.login(new LoginRequest(email, PASSWORD));
    assertThat(login.token()).isNotBlank();
    assertThat(login.user().email()).isEqualTo(email);
  }

  @Test
  void registerRejectsDuplicateEmail() {
    assertThatThrownBy(() -> authService.register(
        new RegisterRequest("Marie", "Dupont", "marie.lectrice@example.fr", PASSWORD, true)))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void loginRejectsBadPassword() {
    assertThatThrownBy(() -> authService.login(
        new LoginRequest("marie.lectrice@example.fr", "wrong-password")))
        .isInstanceOf(BadCredentialsException.class);
  }

}
