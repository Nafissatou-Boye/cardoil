// controller/FideliteController.java
package cardoil.backend.controller;

import cardoil.backend.dto.response.FideliteCompteResponse;
import cardoil.backend.entity.FideliteClient;
import cardoil.backend.repository.FideliteClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fidelite")
@RequiredArgsConstructor
public class FideliteController {

    private final FideliteClientRepository fideliteClientRepository;

    @GetMapping("/accounts/client/{clientId}")
    public ResponseEntity<FideliteCompteResponse> getCompte(
            @PathVariable Long clientId,
            @RequestParam Long companyId) {

        return fideliteClientRepository.findByClientIdAndCompagnieId(clientId, companyId)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private FideliteCompteResponse toDto(FideliteClient f) {
        return FideliteCompteResponse.builder()
                .id(f.getId())
                .clientId(f.getClient().getId())
                .companyId(f.getCompagnie().getId())
                .points(f.getPointsDisponibles())
                .totalEarned(f.getPointsTotal())
                .totalRedeemed(f.getPointsUtilises())
                .lastTransactionDate(f.getDerniereMaj())
                .build();
    }
}