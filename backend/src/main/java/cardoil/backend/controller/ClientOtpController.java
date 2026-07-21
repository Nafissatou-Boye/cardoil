package cardoil.backend.controller;

import cardoil.backend.dto.request.DemandeOtpRequest;
import cardoil.backend.dto.request.VerifierOtpRequest;
import cardoil.backend.service.ClientOtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/otp")
@RequiredArgsConstructor
public class ClientOtpController {

    private final ClientOtpService clientOtpService;

    @PostMapping("/demander")
    public ResponseEntity<Void> demander(@Valid @RequestBody DemandeOtpRequest request) {
        clientOtpService.demanderOtp(request.getTelephone());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verifier")
    public ResponseEntity<Void> verifier(@Valid @RequestBody VerifierOtpRequest request) {
        clientOtpService.verifierOtp(request.getTelephone(), request.getCode());
        return ResponseEntity.ok().build();
    }
}