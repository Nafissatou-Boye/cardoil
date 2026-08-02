package cardoil.backend.controller;

import cardoil.backend.dto.response.RapportTransactionResponse;
import cardoil.backend.service.AdminTransactionService;
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
@PreAuthorize("hasRole('ADMIN_COMPAGNIE')")
public class AdminTransactionController {

    private final AdminTransactionService adminTransactionService;

    @GetMapping("/api/admin/transactions")
    public ResponseEntity<List<RapportTransactionResponse>> getTransactions(
            Authentication authentication,
            @RequestParam String periode,
            @RequestParam(required = false) Long stationId) {
        return ResponseEntity.ok(
                adminTransactionService.getTransactions(authentication.getName(), periode, stationId));
    }
}