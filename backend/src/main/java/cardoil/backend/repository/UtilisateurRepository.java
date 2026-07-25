package cardoil.backend.repository;

import cardoil.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import cardoil.backend.entity.Role;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByLogin(String login);

    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
    long countByActif(boolean actif);
    long countByCompagnieId(Long compagnieId);
List<Utilisateur> findByCompagnieIdAndRole(Long compagnieId, Role role);
Optional<Utilisateur> findByIdAndCompagnieId(Long id, Long compagnieId);
List<Utilisateur> findByRole(Role role);
Optional<Utilisateur> findByIdAndRole(Long id, Role role);
List<Utilisateur> findByCompagnieIdAndRoleIn(Long compagnieId, List<Role> roles);
Optional<Utilisateur> findByEntrepriseIdAndActif(Long entrepriseId, boolean actif);
// À ajouter dans UtilisateurRepository.java, à côté de findByEntrepriseIdAndActif :

Optional<Utilisateur> findByDepartementGereIdAndActif(Long departementGereId, boolean actif);


List<Utilisateur> findByEntrepriseIdAndRoleIn(Long entrepriseId, List<Role> roles);
// À ajouter dans UtilisateurRepository.java :

Optional<Utilisateur> findByTelephone(String telephone);

}