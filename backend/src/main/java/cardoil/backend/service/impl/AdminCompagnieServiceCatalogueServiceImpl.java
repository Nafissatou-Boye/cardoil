package cardoil.backend.service.impl;

import cardoil.backend.dto.request.AssignerStationsRequest;
import cardoil.backend.dto.request.ChangerStatutServiceRequest;
import cardoil.backend.dto.request.ServiceCatalogueRequest;
import cardoil.backend.dto.response.ServiceCatalogueResponse;
import cardoil.backend.entity.Compagnie;
import cardoil.backend.entity.ServiceCatalogue;
import cardoil.backend.entity.Station;
import cardoil.backend.entity.StatutService;
import cardoil.backend.entity.Utilisateur;
import cardoil.backend.repository.ServiceCatalogueRepository;
import cardoil.backend.repository.StationRepository;
import cardoil.backend.repository.UtilisateurRepository;
import cardoil.backend.service.AdminCompagnieServiceCatalogueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCompagnieServiceCatalogueServiceImpl implements AdminCompagnieServiceCatalogueService {

    private final UtilisateurRepository utilisateurRepository;
    private final ServiceCatalogueRepository serviceCatalogueRepository;
    private final StationRepository stationRepository;

    // Résout la compagnie de l'admin connecté — même garde que
    // AdminCompagnieServiceImpl.getDashboard(), pour rester cohérent.
    private Compagnie resolveCompagnie(String login) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        if (utilisateur.getCompagnie() == null) {
            throw new IllegalStateException("Aucune compagnie associée à cet utilisateur");
        }
        return utilisateur.getCompagnie();
    }

    @Override
    public List<ServiceCatalogueResponse> getServices(String login) {
        Compagnie compagnie = resolveCompagnie(login);
        return serviceCatalogueRepository.findByCompagnieId(compagnie.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ServiceCatalogueResponse getService(String login, Long serviceId) {
        Compagnie compagnie = resolveCompagnie(login);
        ServiceCatalogue service = serviceCatalogueRepository.findByIdAndCompagnieId(serviceId, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Service non trouvé pour cette compagnie"));
        return toResponse(service);
    }

    @Override
    @Transactional
    public ServiceCatalogueResponse creerService(String login, ServiceCatalogueRequest request) {
        Compagnie compagnie = resolveCompagnie(login);

        // ✅ Unicité globale du code, pas scopée à la compagnie — cohérent
        // avec @Column(unique = true) sur l'entité (pas de scope compagnie
        // dans la contrainte SQL).
        if (serviceCatalogueRepository.existsByCode(request.getCode())) {
            throw new IllegalStateException("Ce code de service est déjà utilisé");
        }

        // Nouveau service : toujours BROUILLON, jamais actif directement —
        // l'admin doit explicitement le passer à ACTIF via changerStatut()
        // une fois prêt (icône, prix, description vérifiés).
        ServiceCatalogue service = ServiceCatalogue.builder()
                .code(request.getCode())
                .nom(request.getNom())
                .categorie(request.getCategorie())
                .description(request.getDescription())
                .prix(request.getPrix())
                .icone(request.getIcone())
                .couleurHex(request.getCouleurHex())
                .obligatoire(request.isObligatoire())
                .ordreTri(request.getOrdreTri())
                .statut(StatutService.BROUILLON)
                .compagnie(compagnie)
                .build();

        service = serviceCatalogueRepository.save(service);
        return toResponse(service);
    }

    @Override
    @Transactional
    public ServiceCatalogueResponse modifierService(String login, Long serviceId, ServiceCatalogueRequest request) {
        Compagnie compagnie = resolveCompagnie(login);
        ServiceCatalogue service = serviceCatalogueRepository.findByIdAndCompagnieId(serviceId, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Service non trouvé pour cette compagnie"));

        // code volontairement non modifiable après création — identifiant
        // stable, cohérent avec sa contrainte unique.
        service.setNom(request.getNom());
        service.setCategorie(request.getCategorie());
        service.setDescription(request.getDescription());
        service.setPrix(request.getPrix());
        service.setIcone(request.getIcone());
        service.setCouleurHex(request.getCouleurHex());
        service.setObligatoire(request.isObligatoire());
        service.setOrdreTri(request.getOrdreTri());

        service = serviceCatalogueRepository.save(service);
        return toResponse(service);
    }

    @Override
    @Transactional
    public ServiceCatalogueResponse changerStatut(String login, Long serviceId, ChangerStatutServiceRequest request) {
        Compagnie compagnie = resolveCompagnie(login);
        ServiceCatalogue service = serviceCatalogueRepository.findByIdAndCompagnieId(serviceId, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Service non trouvé pour cette compagnie"));

        service.setStatut(request.getStatut());
        service = serviceCatalogueRepository.save(service);
        return toResponse(service);
    }

    @Override
    @Transactional
    public ServiceCatalogueResponse assignerStations(String login, Long serviceId, AssignerStationsRequest request) {
        Compagnie compagnie = resolveCompagnie(login);
        ServiceCatalogue service = serviceCatalogueRepository.findByIdAndCompagnieId(serviceId, compagnie.getId())
                .orElseThrow(() -> new EntityNotFoundException("Service non trouvé pour cette compagnie"));

        List<Long> stationIds = request.getStationIds();
        if (stationIds == null || stationIds.isEmpty()) {
            // ✅ ArrayList mutable, pas List.of() (immuable) — Hibernate a
            // besoin de pouvoir gérer cette collection @ManyToMany en
            // interne pour synchroniser la table de jointure.
            service.setStationsDisponibles(new ArrayList<>());
        } else {
            // ✅ Collecté dans une ArrayList mutable explicitement — .toList()
            // (Stream, Java 16+) renvoie une liste immuable par défaut, même
            // piège que List.of() ci-dessus.
            List<Station> stations = new ArrayList<>(stationIds.stream()
                    .map(id -> stationRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Station introuvable : " + id)))
                    .toList());

            // Sécurité : aucune station d'une autre compagnie ne peut être assignée.
            boolean stationEtrangere = stations.stream()
                    .anyMatch(s -> !s.getCompagnie().getId().equals(compagnie.getId()));
            if (stationEtrangere) {
                throw new IllegalStateException("Une des stations sélectionnées n'appartient pas à votre compagnie");
            }

            service.setStationsDisponibles(stations);
        }

        service = serviceCatalogueRepository.save(service);
        return toResponse(service);
    }

    private ServiceCatalogueResponse toResponse(ServiceCatalogue s) {
        List<Station> stations = s.getStationsDisponibles();
        return ServiceCatalogueResponse.builder()
                .id(s.getId())
                .code(s.getCode())
                .nom(s.getNom())
                .categorie(s.getCategorie() != null ? s.getCategorie().name() : null)
                .description(s.getDescription())
                .prix(s.getPrix())
                .icone(s.getIcone())
                .couleurHex(s.getCouleurHex())
                .statut(s.getStatut().name())
                .obligatoire(s.isObligatoire())
                .ordreTri(s.getOrdreTri())
                .compagnieId(s.getCompagnie().getId())
                .compagnieNom(s.getCompagnie().getNom())
                .stationIds(stations != null ? stations.stream().map(Station::getId).toList() : List.of())
                .stationNoms(stations != null ? stations.stream().map(Station::getNom).toList() : List.of())
                .build();
    }
}