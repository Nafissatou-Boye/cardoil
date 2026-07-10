package cardoil.backend.controller;

import cardoil.backend.dto.request.LiaisonRequestDTO;
import cardoil.backend.dto.response.LiaisonDTO;
import cardoil.backend.enums.StatutEtablissement;
import cardoil.backend.service.LiaisonCompagnieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/etablissements-financiers/{etablissementId}/compagnies")
@RequiredArgsConstructor
public class LiaisonCompagnieController {

    private final LiaisonCompagnieService service;

    @PostMapping
    public ResponseEntity<LiaisonDTO> creer(@PathVariable Long etablissementId,
                                             @RequestBody LiaisonRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creerLiaison(etablissementId, dto));
    }

    @GetMapping
    public ResponseEntity<List<LiaisonDTO>> lister(@PathVariable Long etablissementId) {
        return ResponseEntity.ok(service.listerPourEtablissement(etablissementId));
    }

    @PatchMapping("/{liaisonId}")
    public ResponseEntity<LiaisonDTO> modifierPlafonds(@PathVariable Long etablissementId,
                                                         @PathVariable Long liaisonId,
                                                         @RequestBody LiaisonRequestDTO dto) {
        return ResponseEntity.ok(service.modifierPlafonds(liaisonId, dto));
    }

    @PatchMapping("/{liaisonId}/statut")
    public ResponseEntity<LiaisonDTO> changerStatut(@PathVariable Long etablissementId,
                                                      @PathVariable Long liaisonId,
                                                      @RequestParam StatutEtablissement statut) {
        return ResponseEntity.ok(service.changerStatut(liaisonId, statut));
    }

    @DeleteMapping("/{liaisonId}")
    public ResponseEntity<Void> supprimer(@PathVariable Long etablissementId,
                                           @PathVariable Long liaisonId) {
        service.supprimerLiaison(liaisonId);
        return ResponseEntity.noContent().build();
    }
}