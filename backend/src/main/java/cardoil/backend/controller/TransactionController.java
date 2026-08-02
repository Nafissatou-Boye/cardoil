package cardoil.backend.controller;

import cardoil.backend.dto.request.AchatCarteRequest;
import cardoil.backend.dto.request.AnnulerTransactionRequest;
import cardoil.backend.dto.request.ConfirmerTransactionRequest;
import cardoil.backend.dto.request.InitierTransactionRequest;
import cardoil.backend.dto.request.PayerParQrRequest;
import cardoil.backend.dto.request.RechargeParQrRequest;
import cardoil.backend.dto.request.RechargeRequest;
import cardoil.backend.dto.response.AchatCarteResponse;
import cardoil.backend.dto.response.AnnulerTransactionResponse;
import cardoil.backend.dto.response.ConfirmerTransactionResponse;
import cardoil.backend.dto.response.HistoriqueTransactionResponse;
import cardoil.backend.dto.response.InitierTransactionResponse;
import cardoil.backend.dto.response.PayerParQrResponse;
import cardoil.backend.dto.response.RechargeClientResponse;
import cardoil.backend.dto.response.ResoudreQrResponse;
import cardoil.backend.dto.response.StatsJourResponse;
import cardoil.backend.dto.response.StatutTransactionResponse;
import cardoil.backend.dto.response.TransactionRecenteResponse;
import cardoil.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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

    // Distinct de /api/client/transactions/confirmer : confirmerTransaction
    // débite Client.solde, incompatible avec un Employé dont le solde vit
    // sur Carte. Même mécanisme de code/expiration, débit différent.
    @PostMapping("/api/employe/transactions/confirmer")
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<ConfirmerTransactionResponse> confirmerEmploye(
            Authentication authentication,
            @Valid @RequestBody ConfirmerTransactionRequest request) {
        return ResponseEntity.ok(
                transactionService.confirmerTransactionEmploye(authentication.getName(), request));
    }

    // Sert aussi les employés : getTransactionsClient() n'utilise que
    // Utilisateur.getId() (jamais de cast vers Client), donc leurs achats
    // carte (payerParCarte → transaction.client = carte.getEmploye()) y
    // apparaissent naturellement. Pas de recharge/dotation dans cette liste
    // pour un employé — ce flux est entièrement automatique côté Entreprise.
    @GetMapping("/api/client/transactions")
    @PreAuthorize("hasAnyRole('CLIENT', 'EMPLOYE')")
    public ResponseEntity<List<HistoriqueTransactionResponse>> mesTransactions(
            Authentication authentication) {
        return ResponseEntity.ok(
                transactionService.getTransactionsClient(authentication.getName()));
    }
  

@PostMapping("/api/gerant/transactions/recharge-client/{telephone}")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<RechargeClientResponse> rechargerClient(
        Authentication authentication,
        @PathVariable String telephone,
        @Valid @RequestBody RechargeRequest request) {
    return ResponseEntity.ok(transactionService.rechargerClient(authentication.getName(), telephone, request));
}

// ✅ Nouveau — symétrique de payer-qr, en sens inverse (crédit, pas
// débit). Même rôles autorisés que recharge-client, cohérence avec
// l'existant plutôt qu'une restriction arbitraire au seul Gérant.
@PostMapping("/api/gerant/transactions/recharge-qr")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<RechargeClientResponse> rechargerParQr(
        Authentication authentication,
        @Valid @RequestBody RechargeParQrRequest request) {
    return ResponseEntity.ok(transactionService.rechargerParQr(authentication.getName(), request));
}

@PostMapping("/api/gerant/transactions/achat-carte")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<AchatCarteResponse> payerParCarte(
        Authentication authentication,
        @Valid @RequestBody AchatCarteRequest request) {
    return ResponseEntity.ok(transactionService.payerParCarte(authentication.getName(), request));
}

// Distinct de achat-carte : résout un code QR temporaire scanné (Client ou
// Employé), pas un numeroCarte fixe saisi/lu directement.
@PostMapping("/api/gerant/transactions/payer-qr")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<PayerParQrResponse> payerParQr(
        Authentication authentication,
        @Valid @RequestBody PayerParQrRequest request) {
    return ResponseEntity.ok(transactionService.payerParQr(authentication.getName(), request));
}

// ✅ Nouveau — résolution en lecture seule, avant confirmation. Ne débite
// rien : sert uniquement à afficher "vous allez débiter [porteurNom]"
// avant que payerParQr ne soit réellement appelé.
@GetMapping("/api/gerant/transactions/resoudre-qr")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<ResoudreQrResponse> resoudreQr(@RequestParam String code) {
    return ResponseEntity.ok(transactionService.resoudreQr(code));
}


@GetMapping("/api/gerant/transactions/stats-du-jour")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<StatsJourResponse> statsDuJour(Authentication authentication) {
    return ResponseEntity.ok(transactionService.getStatsDuJour(authentication.getName()));
}

// ✅ Nouveau — la méthode de service existait déjà (TransactionServiceImpl,
// TransactionService) mais aucune route HTTP n'y menait jamais : oubli
// resté invisible tant que personne n'avait besoin de cet endpoint
// précis. Explique le NoResourceFoundException — Spring ne trouvait
// littéralement aucune route à ce chemin, pas un problème de droits ou
// de données.
@GetMapping("/api/gerant/transactions/recentes")
@PreAuthorize("hasAnyRole('GERANT', 'POMPISTE')")
public ResponseEntity<List<TransactionRecenteResponse>> transactionsRecentes(Authentication authentication) {
    return ResponseEntity.ok(transactionService.getTransactionsRecentes(authentication.getName()));
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