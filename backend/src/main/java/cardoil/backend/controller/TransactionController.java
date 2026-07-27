package cardoil.backend.controller;

import cardoil.backend.dto.request.AchatCarteRequest;
import cardoil.backend.dto.request.AnnulerTransactionRequest;
import cardoil.backend.dto.request.ConfirmerTransactionRequest;
import cardoil.backend.dto.request.InitierTransactionRequest;
import cardoil.backend.dto.request.RechargeRequest;
import cardoil.backend.dto.response.AchatCarteResponse;
import cardoil.backend.dto.response.AnnulerTransactionResponse;
import cardoil.backend.dto.response.ConfirmerTransactionResponse;
import cardoil.backend.dto.response.InitierTransactionResponse;
import cardoil.backend.dto.response.RechargeClientResponse;
import cardoil.backend.dto.response.StatsJourResponse;
import cardoil.backend.dto.response.StatutTransactionResponse;
import cardoil.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/api/gerant/transactions/initier")
    @PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
    public ResponseEntity<InitierTransactionResponse> initier(
            Authentication authentication,
            @Valid @RequestBody InitierTransactionRequest request) {
        return ResponseEntity.ok(
                transactionService.initierTransaction(authentication.getName(), request));
    }

    @GetMapping("/api/gerant/transactions/{id}/statut")
    @PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
    public ResponseEntity<StatutTransactionResponse> statut(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                transactionService.getStatut(authentication.getName(), id));
    }

    @PostMapping("/api/client/transactions/confirmer")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ConfirmerTransactionResponse> confirmer(
            Authentication authentication,
            @Valid @RequestBody ConfirmerTransactionRequest request) {
        return ResponseEntity.ok(
                transactionService.confirmerTransaction(authentication.getName(), request));
    }
  

@PostMapping("/api/gerant/transactions/recharge-client/{telephone}")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<RechargeClientResponse> rechargerClient(
        Authentication authentication,
        @PathVariable String telephone,
        @Valid @RequestBody RechargeRequest request) {
    return ResponseEntity.ok(transactionService.rechargerClient(authentication.getName(), telephone, request));
}

@PostMapping("/api/gerant/transactions/achat-carte")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<AchatCarteResponse> payerParCarte(
        Authentication authentication,
        @Valid @RequestBody AchatCarteRequest request) {
    return ResponseEntity.ok(transactionService.payerParCarte(authentication.getName(), request));
}


@GetMapping("/api/gerant/transactions/stats-du-jour")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<StatsJourResponse> statsDuJour(Authentication authentication) {
    return ResponseEntity.ok(transactionService.getStatsDuJour(authentication.getName()));
}

@PostMapping("/api/gerant/transactions/{id}/annuler")
@PreAuthorize("hasRole('GERANT')")  
public ResponseEntity<AnnulerTransactionResponse> annuler(
        Authentication authentication,
        @PathVariable Long id,
        @Valid @RequestBody AnnulerTransactionRequest request) {
    return ResponseEntity.ok(transactionService.annulerTransaction(authentication.getName(), id, request));
}

@GetMapping("/api/gerant/transactions/stats-periode")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<StatsJourResponse> statsPeriode(
        Authentication authentication,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
    return ResponseEntity.ok(transactionService.getStatsPeriode(authentication.getName(), debut, fin));
}
}