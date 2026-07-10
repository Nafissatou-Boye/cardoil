package cardoil.backend.controller;

import cardoil.backend.dto.request.*;
import cardoil.backend.dto.response.*;
import cardoil.backend.enums.StatutEtablissement;
import cardoil.backend.service.EtablissementFinancierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/etablissements-financiers")
@RequiredArgsConstructor
public class SuperAdminEtablissementFinancierController {

    private final EtablissementFinancierService service;

    @PostMapping
    public ResponseEntity<ApiKeyGenereeDTO> creer(@RequestBody EtablissementFinancierCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creerEtablissement(dto));
    }

    @GetMapping
    public ResponseEntity<List<EtablissementFinancierDTO>> lister() {
        return ResponseEntity.ok(service.listerTous());
    }

    @PostMapping("/{id}/rotation-cle")
    public ResponseEntity<ApiKeyGenereeDTO> regenererCle(@PathVariable Long id) {
        return ResponseEntity.ok(service.regenererCle(id));
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<Void> changerStatut(@PathVariable Long id,
                                               @RequestParam StatutEtablissement statut) {
        service.changerStatut(id, statut);
        return ResponseEntity.noContent().build();
    }
}