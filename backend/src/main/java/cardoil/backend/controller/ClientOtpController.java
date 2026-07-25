package cardoil.backend.controller;

import cardoil.backend.dto.request.DemandeOtpRequest;
import cardoil.backend.dto.request.InscriptionClientRequest;
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
}