package cardoil.backend.controller;

import cardoil.backend.service.PrixJourBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

// ⚠ Chemin provisoire — voir ma question à la fin du message
@RestController
@RequestMapping("/api/admin/prix-jour")
@RequiredArgsConstructor
public class PrixJourBatchController {

    private final PrixJourBatchService batchService;

    @PostMapping("/rejeu")
    public ResponseEntity<PrixJourBatchService.ResultatBatch> rejouer(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(batchService.alimenterPrixJourPourDate(date));
    }
}