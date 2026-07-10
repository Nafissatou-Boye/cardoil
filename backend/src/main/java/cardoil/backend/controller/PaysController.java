package cardoil.backend.controller;

import cardoil.backend.dto.request.PaysRequest;
import cardoil.backend.dto.response.PaysResponse;
import cardoil.backend.service.PaysService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pays")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PaysController {

    private final PaysService paysService;

    @GetMapping
    public ResponseEntity<List<PaysResponse>> getAll() {
        return ResponseEntity.ok(paysService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaysResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paysService.getById(id));
    }

    @PostMapping
    public ResponseEntity<PaysResponse> create(@Valid @RequestBody PaysRequest request) {
        return ResponseEntity.ok(paysService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaysResponse> update(@PathVariable Long id, @Valid @RequestBody PaysRequest request) {
        return ResponseEntity.ok(paysService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paysService.delete(id);
        return ResponseEntity.noContent().build();
    }
}