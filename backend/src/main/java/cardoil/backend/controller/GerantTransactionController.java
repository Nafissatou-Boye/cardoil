package cardoil.backend.controller;

import cardoil.backend.dto.request.TransactionRequest;
import cardoil.backend.dto.response.ProduitResponse;
import cardoil.backend.dto.response.TransactionResponse;
import cardoil.backend.service.GerantTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gerant/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GERANT')")
public class GerantTransactionController {

    private final GerantTransactionService gerantTransactionService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getRecentes(Authentication authentication) {
        return ResponseEntity.ok(gerantTransactionService.getRecentes(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            Authentication authentication,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(gerantTransactionService.create(authentication.getName(), request));
    }

    @GetMapping("/produits-disponibles")
    public ResponseEntity<List<ProduitResponse>> getProduitsDisponibles(Authentication authentication) {
        return ResponseEntity.ok(gerantTransactionService.getProduitsDisponibles(authentication.getName()));
    }
}