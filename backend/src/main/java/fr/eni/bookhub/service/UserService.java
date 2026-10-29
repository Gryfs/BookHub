package fr.eni.bookhub.service;

import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.dto.ChangePasswordRequest;
import fr.eni.bookhub.dto.ProfileResponse;
import fr.eni.bookhub.dto.UpdateProfileRequest;
import fr.eni.bookhub.repository.LoanRepository;
import fr.eni.bookhub.repository.UserRepository;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import fr.eni.bookhub.service.exception.InvalidFieldException;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private static final List<LoanStatus> CURRENT_LOAN_STATUSES = List.of(LoanStatus.ACTIVE, LoanStatus.LATE);

  private final UserRepository userRepository;
  private final LoanRepository loanRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public ProfileResponse getProfile(String email) {
    return ProfileResponse.from(findUser(email));
  }

  @Transactional
  public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
    User user = findUser(email);
    user.setFirstName(request.firstName().trim());
    user.setLastName(request.lastName().trim());
    user.setPhone(emptyToNull(request.phone()));
    user.setPseudonym(emptyToNull(request.pseudonym()));
    return ProfileResponse.from(user);
  }

  @Transactional
  public void changePassword(String email, ChangePasswordRequest request) {
    User user = findUser(email);

    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new InvalidFieldException("currentPassword", "Le mot de passe actuel est incorrect.");
    }
    if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
      throw new InvalidFieldException("newPassword",
          "Le nouveau mot de passe doit être différent de l'ancien.");
    }

    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
  }

  @Transactional
  public void deleteAccount(String email) {
    User user = findUser(email);

    if (loanRepository.existsByUserIdAndStatusIn(user.getId(), CURRENT_LOAN_STATUSES)) {
      throw new BusinessRuleException(
          "Vous ne pouvez pas supprimer votre compte tant qu'un emprunt est en cours.");
    }

    userRepository.anonymize(user.getId());
  }

  private User findUser(String email) {
    return userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
  }

  private String emptyToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

}
