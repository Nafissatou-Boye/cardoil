// ProduitController.java — nouveau
package cardoil.backend.controller;

import cardoil.backend.dto.response.ProduitOptionResponse;
import cardoil.backend.entity.*;
import cardoil.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gerant/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final UtilisateurRepository utilisateurRepository;
    private final StationRepository stationRepository;
    private final ProduitRepository produitRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
    public ResponseEntity<List<ProduitOptionResponse>> mesProduits(Authentication authentication) {
        Utilisateur operateur = utilisateurRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));

        Station station = resolveStation(operateur);
        List<Produit> produits = produitRepository.findByCompagnieId(station.getCompagnie().getId());

        List<ProduitOptionResponse> response = produits.stream()
                .map(p -> ProduitOptionResponse.builder().id(p.getId()).nom(p.getNom()).build())
                .toList();

        return ResponseEntity.ok(response);
    }

    private Station resolveStation(Utilisateur operateur) {
        if (operateur.getRole() == Role.GERANT) {
            return stationRepository.findByGerantId(operateur.getId()).stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Aucune station assignée à ce gérant"));
        }
        if (operateur.getRole() == Role.POMPISTE) {
            if (operateur.getStation() == null) {
                throw new IllegalStateException("Aucune station assignée à ce pompiste");
            }
            return operateur.getStation();
        }
        throw new IllegalStateException("Rôle non autorisé");
    }
}