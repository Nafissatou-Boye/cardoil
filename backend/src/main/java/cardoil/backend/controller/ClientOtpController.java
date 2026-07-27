package cardoil.backend.controller;

import cardoil.backend.dto.request.DemandeOtpRequest;
import cardoil.backend.dto.request.InscriptionClientRequest;
import cardoil.backend.dto.request.MotDePasseOublieRequest;
import cardoil.backend.dto.request.ReinitialiserMotDePasseRequest;
import cardoil.backend.dto.request.VerifierCodeResetRequest;
import cardoil.backend.dto.request.VerifierOtpRequest;
import cardoil.backend.dto.response.CompagnieOptionResponse;
import cardoil.backend.service.ClientOtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/otp")
@RequiredArgsConstructor
public class ClientOtpController {

    private final ClientOtpService clientOtpService;

    @GetMapping("/compagnies")
    public ResponseEntity<List<CompagnieOptionResponse>> getCompagnies() {
        return ResponseEntity.ok(clientOtpService.getCompagniesDisponibles());
    }

    @PostMapping("/inscrire")
    public ResponseEntity<Void> inscrire(@Valid @RequestBody InscriptionClientRequest request) {
        clientOtpService.inscrire(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/renvoyer")
    public ResponseEntity<Void> renvoyer(@Valid @RequestBody DemandeOtpRequest request) {
        clientOtpService.renvoyerOtp(request.getTelephone());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verifier")
    public ResponseEntity<Void> verifier(@Valid @RequestBody VerifierOtpRequest request) {
        clientOtpService.verifierOtp(request.getTelephone(), request.getCode());
        return ResponseEntity.ok().build();
    }

    // ===== MOT DE PASSE OUBLIÉ =====

    @PostMapping("/mot-de-passe-oublie")
    public ResponseEntity<Void> motDePasseOublie(@Valid @RequestBody MotDePasseOublieRequest request) {
        clientOtpService.demanderResetMotDePasse(request.getTelephone());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verifier-code-reset")
    public ResponseEntity<Void> verifierCodeReset(@Valid @RequestBody VerifierCodeResetRequest request) {
        clientOtpService.verifierCodeReset(request.getTelephone(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reinitialiser-mot-de-passe")
    public ResponseEntity<Void> reinitialiserMotDePasse(@Valid @RequestBody ReinitialiserMotDePasseRequest request) {
        clientOtpService.reinitialiserMotDePasse(
                request.getTelephone(), request.getCode(), request.getNouveauMotDePasse());
        return ResponseEntity.ok().build();
    }
}