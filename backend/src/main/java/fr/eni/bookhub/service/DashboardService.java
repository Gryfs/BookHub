package fr.eni.bookhub.service;

import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.domain.Role;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.dto.AdminDashboardResponse;
import fr.eni.bookhub.dto.LibrarianDashboardResponse;
import fr.eni.bookhub.dto.LibraryStatsResponse;
import fr.eni.bookhub.dto.LoanResponse;
import fr.eni.bookhub.dto.ReaderDashboardResponse;
import fr.eni.bookhub.dto.TopBookStatResponse;
import fr.eni.bookhub.dto.UserAdminResponse;
import fr.eni.bookhub.repository.UserRepository;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private static final int RECENT_RETURNS_LIMIT = 5;
  private static final int TOP_BOOKS_LIMIT = 10;
  private static final int TOP_BOOKS_MONTHS = 12;

  private final EntityManager entityManager;
  private final LoanService loanService;
  private final ReservationService reservationService;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public ReaderDashboardResponse readerDashboard(String email) {
    var loans = loanService.getMyLoans(email);
    var reservations = reservationService.listMine(email);
    boolean hasLate = loans.current().stream().anyMatch(l -> l.status() == LoanStatus.LATE);
    List<LoanResponse> recent = loans.history().stream().limit(RECENT_RETURNS_LIMIT).toList();
    return new ReaderDashboardResponse(loans.current(), reservations, hasLate, recent);
  }

  @Transactional(readOnly = true)
  public LibrarianDashboardResponse librarianDashboard() {
    return new LibrarianDashboardResponse(libraryStats(), topBooks(TOP_BOOKS_LIMIT, TOP_BOOKS_MONTHS));
  }

  @Transactional(readOnly = true)
  public AdminDashboardResponse adminDashboard() {
    long users = userRepository.findAll().stream()
        .filter(u -> !isAnonymized(u))
        .count();
    return new AdminDashboardResponse(
        libraryStats(),
        topBooks(TOP_BOOKS_LIMIT, TOP_BOOKS_MONTHS),
        users);
  }

  @Transactional(readOnly = true)
  public List<UserAdminResponse> listUsersForAdmin() {
    return userRepository.findAll().stream()
        .filter(u -> !isAnonymized(u))
        .sorted(Comparator.comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER))
        .map(UserAdminResponse::from)
        .toList();
  }

  @Transactional
  public UserAdminResponse updateUserRole(String adminEmail, Long userId, Role newRole) {
    User admin = userRepository.findByEmailIgnoreCase(adminEmail)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    User target = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", userId));

    if (isAnonymized(target)) {
      throw new BusinessRuleException("Ce compte n'est plus modifiable.");
    }

    if (target.getRole() == Role.ADMIN && newRole != Role.ADMIN
        && userRepository.countByRole(Role.ADMIN) <= 1) {
      throw new BusinessRuleException("Impossible de retirer le dernier administrateur.");
    }

    if (target.getId().equals(admin.getId()) && newRole != Role.ADMIN) {
      throw new BusinessRuleException("Vous ne pouvez pas retirer votre propre rôle administrateur.");
    }

    target.setRole(newRole);
    return UserAdminResponse.from(target);
  }

  public LibraryStatsResponse libraryStats() {
    Object[] row = (Object[]) entityManager
        .createNativeQuery("SELECT * FROM fn_stats_bibliotheque()")
        .getSingleResult();
    return new LibraryStatsResponse(
        toLong(row[0]),
        toLong(row[1]),
        toLong(row[2]),
        toLong(row[3]),
        toLong(row[4]));
  }

  @SuppressWarnings("unchecked")
  public List<TopBookStatResponse> topBooks(int limit, int months) {
    List<Object[]> rows = entityManager
        .createNativeQuery("SELECT * FROM fn_top_livres(?1, ?2)")
        .setParameter(1, limit)
        .setParameter(2, months)
        .getResultList();
    return rows.stream()
        .map(r -> new TopBookStatResponse(
            toLong(r[0]),
            (String) r[1],
            (String) r[2],
            toLong(r[3])))
        .toList();
  }

  private static boolean isAnonymized(User user) {
    return user.getEmail() != null && user.getEmail().startsWith("deleted-");
  }

  private static long toLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    return 0L;
  }

}
