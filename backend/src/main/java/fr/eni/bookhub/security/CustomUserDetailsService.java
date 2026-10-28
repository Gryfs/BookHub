package fr.eni.bookhub.security;

import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) {
    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));

    return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
        .password(user.getPasswordHash())
        .roles(user.getRole().name())
        .build();
  }

}
