package cardoil.backend.service;

import cardoil.backend.entity.EtablissementFinancier;
import cardoil.backend.enums.StatutEtablissement;
import cardoil.backend.exception.CardoilException;
import cardoil.backend.dto.request.EtablissementFinancierCreateDTO;
import cardoil.backend.dto.response.ApiKeyGenereeDTO;
import cardoil.backend.dto.response.EtablissementFinancierDTO;
import cardoil.backend.repository.EtablissementFinancierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EtablissementFinancierService {

    private final EtablissementFinancierRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public ApiKeyGenereeDTO creerEtablissement(EtablissementFinancierCreateDTO dto) {
        if (repository.existsByCode(dto.getCode())) {
            throw new CardoilException("Le code établissement existe déjà : " + dto.getCode());
        }

        String cleGeneree = genererCle();

        EtablissementFinancier etablissement = EtablissementFinancier.builder()
                .nom(dto.getNom())
                .code(dto.getCode())
                .type(dto.getType())
                .statut(StatutEtablissement.ACTIF)
                .apiKeyHash(encoder.encode(cleGeneree))
                .apiKeyPrefix(cleGeneree.substring(0, 12))
                .apiKeyExpiration(LocalDateTime.now().plusYears(1))
                .rateLimitParMinute(dto.getRateLimitParMinute() != null ? dto.getRateLimitParMinute() : 60)
                .emailContact(dto.getEmailContact())
                .telephoneContact(dto.getTelephoneContact())
                .build();

        EtablissementFinancier sauvegarde = repository.save(etablissement);

        return ApiKeyGenereeDTO.builder()
                .etablissementId(sauvegarde.getId())
                .apiKey(cleGeneree)
                .apiKeyPrefix(sauvegarde.getApiKeyPrefix())
                .build();
    }

    @Transactional
    public ApiKeyGenereeDTO regenererCle(Long etablissementId) {
        EtablissementFinancier etablissement = repository.findById(etablissementId)
                .orElseThrow(() -> new CardoilException("Établissement introuvable"));

        String nouvelleCle = genererCle();
        etablissement.setApiKeyHash(encoder.encode(nouvelleCle));
        etablissement.setApiKeyPrefix(nouvelleCle.substring(0, 12));
        etablissement.setApiKeyExpiration(LocalDateTime.now().plusYears(1));
        etablissement.setDateDerniereRotationCle(LocalDateTime.now());
        repository.save(etablissement);

        return ApiKeyGenereeDTO.builder()
                .etablissementId(etablissement.getId())
                .apiKey(nouvelleCle)
                .apiKeyPrefix(etablissement.getApiKeyPrefix())
                .build();
    }

    @Transactional
    public void changerStatut(Long etablissementId, StatutEtablissement nouveauStatut) {
        EtablissementFinancier etablissement = repository.findById(etablissementId)
                .orElseThrow(() -> new CardoilException("Établissement introuvable"));
        etablissement.setStatut(nouveauStatut);
        repository.save(etablissement);
    }

    public List<EtablissementFinancierDTO> listerTous() {
        return repository.findAll().stream().map(this::versDTO).toList();
    }

    private String genererCle() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "cdk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private EtablissementFinancierDTO versDTO(EtablissementFinancier e) {
        return EtablissementFinancierDTO.builder()
                .id(e.getId())
                .nom(e.getNom())
                .code(e.getCode())
                .type(e.getType())
                .statut(e.getStatut())
                .apiKeyPrefix(e.getApiKeyPrefix())
                .apiKeyExpiration(e.getApiKeyExpiration())
                .rateLimitParMinute(e.getRateLimitParMinute())
                .emailContact(e.getEmailContact())
                .dateCreation(e.getDateCreation())
                .nombreCompagniesLiees(e.getLiaisonsCompagnies().size())
                .build();
    }
}