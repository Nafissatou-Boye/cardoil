package cardoil.backend.controller;

import cardoil.backend.dto.response.RechargeListItemDTO;
import cardoil.backend.dto.response.RechargeStatsDTO;
import cardoil.backend.dto.response.RechargeStatsParPartenaireDTO;
import cardoil.backend.enums.StatutRecharge;
import cardoil.backend.service.RechargeSupervisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/super-admin/recharge-externe")
@RequiredArgsConstructor
public class RechargeSupervisionController {

    private final RechargeSupervisionService service;

    @GetMapping
    public ResponseEntity<Page<RechargeListItemDTO>> lister(
            @RequestParam(required = false) Long etablissementId,
            @RequestParam(required = false) Long compagnieId,
            @RequestParam(required = false) StatutRecharge statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(service.lister(
                etablissementId, compagnieId, statut,
                debutJournee(dateDebut), finJournee(dateFin),
                pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<RechargeStatsDTO> stats(
            @RequestParam(required = false) Long etablissementId,
            @RequestParam(required = false) Long compagnieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        return ResponseEntity.ok(service.obtenirStats(
                etablissementId, compagnieId, debutJournee(dateDebut), finJournee(dateFin)));
    }

    @GetMapping("/stats/par-partenaire")
    public ResponseEntity<List<RechargeStatsParPartenaireDTO>> statsParPartenaire(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        return ResponseEntity.ok(service.obtenirStatsParPartenaire(debutJournee(dateDebut), finJournee(dateFin)));
    }

    private LocalDateTime debutJournee(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime finJournee(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59) : null;
    }
}