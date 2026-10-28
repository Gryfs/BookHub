package fr.eni.bookhub.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.eni.bookhub.domain.Role;
import fr.eni.bookhub.domain.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
  private static final String OTHER_SECRET = "ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=";

  private User createUser() {
    User user = new User();
    user.setEmail("marie.lectrice@example.fr");
    user.setRole(Role.USER);
    return user;
  }

  @Test
  void generatedTokenContainsEmail() {
    JwtService jwtService = new JwtService(SECRET, 24);

    String token = jwtService.generateToken(createUser());

    assertThat(jwtService.extractEmail(token)).isEqualTo("marie.lectrice@example.fr");
  }

  @Test
  void expiredTokenIsRejected() {
    JwtService jwtService = new JwtService(SECRET, -1);

    String token = jwtService.generateToken(createUser());

    assertThatThrownBy(() -> jwtService.extractEmail(token)).isInstanceOf(ExpiredJwtException.class);
  }

  @Test
  void tokenSignedWithAnotherKeyIsRejected() {
    String token = new JwtService(OTHER_SECRET, 24).generateToken(createUser());

    assertThatThrownBy(() -> new JwtService(SECRET, 24).extractEmail(token))
        .isInstanceOf(SignatureException.class);
  }

}
