package cardoil.backend.repository;

import cardoil.backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByTelephone(String telephone);
    boolean existsByTelephone(String telephone);
    // À ajouter dans ClientRepository.java :

Optional<Client> findByNomUtilisateur(String nomUtilisateur);
boolean existsByNomUtilisateur(String nomUtilisateur);
// À ajouter dans ClientRepository.java (remplace les 2 lignes ajoutées précédemment si déjà présentes) :

Optional<Client> findByNomUtilisateurIgnoreCase(String nomUtilisateur);
boolean existsByNomUtilisateurIgnoreCase(String nomUtilisateur);
}