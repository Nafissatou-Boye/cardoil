package cardoil.backend.controller;

import cardoil.backend.dto.request.CompagnieRequest;
import cardoil.backend.dto.response.CompagnieResponse;
import cardoil.backend.service.CompagnieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compagnies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CompagnieController {

    private final CompagnieService compagnieService;

    @GetMapping
    public ResponseEntity<List<CompagnieResponse>> getAll() {
        return ResponseEntity.ok(compagnieService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompagnieResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(compagnieService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CompagnieResponse> create(@Valid @RequestBody CompagnieRequest request) {
        return ResponseEntity.ok(compagnieService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompagnieResponse> update(@PathVariable Long id, @Valid @RequestBody CompagnieRequest request) {
        return ResponseEntity.ok(compagnieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        compagnieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}