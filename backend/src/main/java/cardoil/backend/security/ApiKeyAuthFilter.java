package cardoil.backend.security;

import cardoil.backend.dto.response.ErreurRechargeDTO;
import cardoil.backend.entity.EtablissementFinancier;
import cardoil.backend.enums.StatutEtablissement;
import cardoil.backend.repository.EtablissementFinancierRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final EtablissementFinancierRepository etablissementRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiKeyAuthFilter(EtablissementFinancierRepository etablissementRepository) {
        this.etablissementRepository = etablissementRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            envoyerErreur(response, 401, "ERR_UNAUTHORIZED", "Clé API absente ou mal formée.");
            return;
        }
        String cleFournie = authHeader.substring(7);

        if (cleFournie.length() < 12) {
            envoyerErreur(response, 401, "ERR_UNAUTHORIZED", "Clé API invalide.");
            return;
        }
        String prefixe = cleFournie.substring(0, 12);

        List<EtablissementFinancier> candidats = etablissementRepository.findAll().stream()
                .filter(e -> prefixe.equals(e.getApiKeyPrefix()))
                .toList();

        EtablissementFinancier etablissement = candidats.stream()
                .filter(e -> encoder.matches(cleFournie, e.getApiKeyHash()))
                .findFirst()
                .orElse(null);

        if (etablissement == null) {
            envoyerErreur(response, 401, "ERR_UNAUTHORIZED", "Clé API invalide.");
            return;
        }
        if (etablissement.getApiKeyExpiration() != null &&
                etablissement.getApiKeyExpiration().isBefore(LocalDateTime.now())) {
            envoyerErreur(response, 401, "ERR_UNAUTHORIZED", "Clé API expirée.");
            return;
        }
        if (etablissement.getStatut() != StatutEtablissement.ACTIF) {
            envoyerErreur(response, 403, "ERR_PARTNER_INACTIVE",
                    "L'établissement financier est suspendu ou non autorisé.");
            return;
        }

        request.setAttribute("etablissementFinancier", etablissement);
        filterChain.doFilter(request, response);
    }

    private void envoyerErreur(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        ErreurRechargeDTO erreur = ErreurRechargeDTO.builder().errorCode(code).message(message).build();
        response.getWriter().write(objectMapper.writeValueAsString(erreur));
    }
}