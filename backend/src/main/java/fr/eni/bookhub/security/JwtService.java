package fr.eni.bookhub.security;

import fr.eni.bookhub.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expirationHours;

  public JwtService(@Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-hours}") long expirationHours) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    this.expirationHours = expirationHours;
  }

  public String generateToken(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("role", user.getRole().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public String extractEmail(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

}
