package cardoil.backend.controller;

import cardoil.backend.dto.request.*;
import cardoil.backend.dto.response.*;
import cardoil.backend.entity.EtablissementFinancier;
import cardoil.backend.service.RechargeExterneService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Endpoint public partenaire — sécurisé par ApiKeyAuthFilter, pas par JWT
@RestController
@RequestMapping("/api/v1/recharge")
@RequiredArgsConstructor
public class RechargeExterneApiController {

    private final RechargeExterneService service;

    @PostMapping
    public ResponseEntity<RechargeResponseDTO> recharger(@Valid @RequestBody RechargeRequestDTO requete,
                                                           HttpServletRequest httpRequest) {
        EtablissementFinancier etablissement =
                (EtablissementFinancier) httpRequest.getAttribute("etablissementFinancier");
        String ip = httpRequest.getRemoteAddr();

        RechargeResponseDTO reponse = service.traiterRecharge(requete, etablissement, ip);
        return ResponseEntity.ok(reponse);
    }
}