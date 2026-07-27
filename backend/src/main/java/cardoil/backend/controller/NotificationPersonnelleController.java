// NotificationPersonnelleController.java
package cardoil.backend.controller;

import cardoil.backend.dto.response.NotificationPersonnelleResponse;
import cardoil.backend.service.NotificationPersonnelleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationPersonnelleController {

    private final NotificationPersonnelleService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationPersonnelleResponse>> mesNotifications(Authentication auth) {
        return ResponseEntity.ok(notificationService.mesNotifications(auth.getName()));
    }

    @GetMapping("/compte-non-lues")
    public ResponseEntity<Map<String, Long>> compteNonLues(Authentication auth) {
        return ResponseEntity.ok(Map.of("compteNonLues", notificationService.compteNonLues(auth.getName())));
    }

    @PatchMapping("/{id}/lu")
    public ResponseEntity<Void> marquerLue(Authentication auth, @PathVariable Long id) {
        notificationService.marquerLue(auth.getName(), id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/tout-lire")
    public ResponseEntity<Void> marquerToutLu(Authentication auth) {
        notificationService.marquerToutLu(auth.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(Authentication auth, @PathVariable Long id) {
        notificationService.supprimer(auth.getName(), id);
        return ResponseEntity.ok().build();
    }
}