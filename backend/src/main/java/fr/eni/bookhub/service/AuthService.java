package fr.eni.bookhub.service;

import fr.eni.bookhub.domain.Role;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.dto.LoginRequest;
import fr.eni.bookhub.dto.LoginResponse;
import fr.eni.bookhub.dto.RegisterRequest;
import fr.eni.bookhub.dto.UserResponse;
import fr.eni.bookhub.repository.UserRepository;
import fr.eni.bookhub.security.JwtService;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Transactional
  public UserResponse register(RegisterRequest request) {
    String email = request.email().trim().toLowerCase();
    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new BusinessRuleException("Cette adresse email est déjà utilisée.");
    }

    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setFirstName(request.firstName().trim());
    user.setLastName(request.lastName().trim());
    user.setRole(Role.USER);

    return UserResponse.from(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    User user = userRepository.findByEmailIgnoreCase(request.email().trim())
        .orElseThrow(() -> new BadCredentialsException("Identifiants incorrects"));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new BadCredentialsException("Identifiants incorrects");
    }

    return new LoginResponse(jwtService.generateToken(user), UserResponse.from(user));
  }

}
