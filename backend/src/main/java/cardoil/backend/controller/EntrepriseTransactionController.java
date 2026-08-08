package cardoil.backend.controller;

import cardoil.backend.dto.response.RapportTransactionEmployeResponse;
import cardoil.backend.service.EntrepriseTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_ENTREPRISE')")
public class EntrepriseTransactionController {

    private final EntrepriseTransactionService entrepriseTransactionService;

    @GetMapping("/api/entreprise/transactions")
    public ResponseEntity<List<RapportTransactionEmployeResponse>> getTransactions(
            Authentication authentication,
            @RequestParam String periode) {
        return ResponseEntity.ok(
                entrepriseTransactionService.getTransactions(authentication.getName(), periode));
    }
}