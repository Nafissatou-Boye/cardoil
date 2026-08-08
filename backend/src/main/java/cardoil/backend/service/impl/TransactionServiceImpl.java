package cardoil.backend.service.impl;

import cardoil.backend.dto.request.*;
import cardoil.backend.dto.response.*;
import cardoil.backend.entity.*;
import cardoil.backend.repository.*;
import cardoil.backend.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final StationRepository stationRepository;
    private final ServiceCatalogueRepository serviceCatalogueRepository;
    private final FideliteClientRepository fideliteClientRepository;
    private final CarteRepository carteRepository;
    private final NotificationPersonnelleRepository notificationRepository;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final int DUREE_VALIDITE_MINUTES = 5;
    private static final BigDecimal MONTANT_PAR_POINT = BigDecimal.valueOf(1000);

    @Override
    @Transactional
    public InitierTransactionResponse initierTransaction(String loginOperateur, InitierTransactionRequest request) {
        Utilisateur operateur = utilisateurRepository.findByLogin(loginOperateur)
                .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));

        Station station = resolveStation(operateur);
        Long compagnieId = station.getCompagnie().getId();

        Produit produit = null;
        ServiceCatalogue service = null;
        String nomArticle;

        if (request.getProduitId() != null) {
            produit = produitRepository.findByIdAndCompagnieId(request.getProduitId(), compagnieId)
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé pour cette compagnie"));
            nomArticle = produit.getNom();
        } else {
            service = serviceCatalogueRepository.findByIdAndCompagnieId(request.getServiceId(), compagnieId)
                    .orElseThrow(() -> new EntityNotFoundException("Service non trouvé pour cette compagnie"));
            if (service.getStatut() != StatutService.ACTIF) {
                throw new IllegalStateException("Ce service n'est pas actif");
            }
            if (!estDisponibleDansStation(service, station)) {
                throw new IllegalStateException("Ce service n'est pas disponible dans cette station");
            }
            nomArticle = service.getNom();
        }

        String code = genererCodeUnique();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(DUREE_VALIDITE_MINUTES);

        Transaction transaction = Transaction.builder()
                .montant(request.getMontant())
                .type(TypeTransaction.ACHAT)
                .statut(StatutTransaction.EN_ATTENTE)
                .produit(produit)
                .service(service)
                .station(station)
                .operateur(operateur)
                .reference(genererReference())
                .codeConfirmation(code)
                .codeConfirmationExpiration(expiration)
                .build();

        transaction = transactionRepository.save(transaction);

        return InitierTransactionResponse.builder()
                .transactionId(transaction.getId())
                .codeConfirmation(code)
                .expiration(expiration)
                .montant(request.getMontant())
                .produitNom(nomArticle)
                .stationNom(station.getNom())
                .build();
    }

    private Station resolveStation(Utilisateur operateur) {
        if (operateur.getRole() == Role.GERANT) {
            return stationRepository.findByGerantId(operateur.getId()).stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Aucune station assignée à ce gérant"));
        }
        if (operateur.getRole() == Role.POMPISTE) {
            if (operateur.getStation() == null) {
                throw new IllegalStateException("Aucune station assignée à ce pompiste");
            }
            return operateur.getStation();
        }
        throw new IllegalStateException("Rôle non autorisé à initier une transaction");
    }

    private boolean estDisponibleDansStation(ServiceCatalogue service, Station station) {
        if (service.isObligatoire()) return true;
        List<Station> stations = service.getStationsDisponibles();
        if (stations == null || stations.isEmpty()) return true;
        return stations.stream().anyMatch(s -> s.getId().equals(station.getId()));
    }

    @Override
    @Transactional(noRollbackFor = IllegalStateException.class)
    public ConfirmerTransactionResponse confirmerTransaction(String loginClient, ConfirmerTransactionRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(loginClient)
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé"));

        if (!(utilisateur instanceof Client client)) {
            throw new IllegalStateException("Cette action est réservée aux clients particuliers");
        }

        Transaction transaction = transactionRepository.findByCodeConfirmation(request.getCode())
                .orElseThrow(() -> new EntityNotFoundException("Code invalide"));

        if (transaction.getStatut() != StatutTransaction.EN_ATTENTE) {
            throw new IllegalStateException("Cette transaction a déjà été traitée");
        }
        if (transaction.getCodeConfirmationExpiration().isBefore(LocalDateTime.now())) {
            transaction.setStatut(StatutTransaction.ECHEC);
            transactionRepository.save(transaction);
            throw new IllegalStateException("Code expiré, demandez à l'opérateur de réessayer");
        }

        BigDecimal montant = transaction.getMontant();

        if (client.getSolde().compareTo(montant) < 0) {
            transaction.setStatut(StatutTransaction.ECHEC);
            transaction.setClient(client);
            transactionRepository.save(transaction);
            throw new IllegalStateException("Solde insuffisant");
        }

        client.setSolde(client.getSolde().subtract(montant));
        clientRepository.save(client);

        int pointsGagnes = crediterPointsFidelite(client, transaction.getStation().getCompagnie(), montant);

        transaction.setClient(client);
        transaction.setStatut(StatutTransaction.REUSSIE);
        transaction.setCodeConfirmation(null);
        transaction.setPointsGagnes(pointsGagnes);
        transaction = transactionRepository.save(transaction);

        String nomArticle = transaction.getProduit() != null
                ? transaction.getProduit().getNom()
                : (transaction.getService() != null ? transaction.getService().getNom() : null);

        creerNotification(client, "Paiement réussi",
                "Votre paiement de " + montant + " FCFA a été confirmé.",
                TypeNotificationPersonnelle.PAIEMENT_REUSSI, transaction);

        return ConfirmerTransactionResponse.builder()
                .transactionId(transaction.getId())
                .reference(transaction.getReference())
                .montant(montant)
                .produitNom(nomArticle)
                .stationNom(transaction.getStation().getNom())
                .nouveauSolde(client.getSolde())
                .statut(transaction.getStatut().name())
                .dateTransaction(transaction.getDateTransaction())
                .pointsGagnes(pointsGagnes)
                .build();
    }

    private int crediterPointsFidelite(Client client, Compagnie compagnie, BigDecimal montant) {
        int pointsGagnes = montant.divideToIntegralValue(MONTANT_PAR_POINT).intValue();
        if (pointsGagnes <= 0) return 0;

        FideliteClient fidelite = fideliteClientRepository
                .findByClientIdAndCompagnieId(client.getId(), compagnie.getId())
                .orElseGet(() -> FideliteClient.builder()
                        .client(client)
                        .compagnie(compagnie)
                        .build());

        fidelite.setPointsTotal(fidelite.getPointsTotal() + pointsGagnes);
        fidelite.setPointsDisponibles(fidelite.getPointsDisponibles() + pointsGagnes);
        fideliteClientRepository.save(fidelite);

        return pointsGagnes;
    }

    private void retirerPointsFidelite(Client client, Compagnie compagnie, int points) {
        fideliteClientRepository.findByClientIdAndCompagnieId(client.getId(), compagnie.getId())
                .ifPresent(fidelite -> {
                    fidelite.setPointsDisponibles(Math.max(0, fidelite.getPointsDisponibles() - points));
                    fidelite.setPointsTotal(Math.max(0, fidelite.getPointsTotal() - points));
                    fideliteClientRepository.save(fidelite);
                });
    }

    @Override
    @Transactional(noRollbackFor = IllegalStateException.class)
    public RechargeClientResponse rechargerClient(String loginOperateur, String telephone, RechargeRequest request) {
        Utilisateur operateur = utilisateurRepository.findByLogin(loginOperateur)
                .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));
        Station station = resolveStation(operateur);

        Client client = clientRepository.findByTelephone(telephone)
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé pour ce téléphone"));

        Transaction transaction = Transaction.builder()
                .montant(request.getMontant())
                .type(TypeTransaction.RECHARGE)
                .statut(StatutTransaction.REUSSIE)
                .station(station)
                .operateur(operateur)
                .client(client)
                .reference(genererReference())
                .build();
        transaction = transactionRepository.save(transaction);

        client.setSolde(client.getSolde().add(request.getMontant()));
        clientRepository.save(client);

        creerNotification(client, "Recharge reçue",
                "Votre compte a été crédité de " + request.getMontant() + " FCFA.",
                TypeNotificationPersonnelle.RECHARGE_REUSSIE, transaction);

        return RechargeClientResponse.builder()
                .transactionId(transaction.getId())
                .reference(transaction.getReference())
                .montant(request.getMontant())
                .clientNom(client.getPrenom() + " " + client.getNom())
                .telephoneMasque(masquerTelephone(client.getTelephone()))
                .nouveauSolde(client.getSolde())
                .statut(transaction.getStatut().name())
                .stationNom(station.getNom())
                .dateTransaction(transaction.getDateTransaction())
                .build();
    }

    @Override
    @Transactional(noRollbackFor = IllegalStateException.class)
    public AchatCarteResponse payerParCarte(String loginOperateur, AchatCarteRequest request) {
        Utilisateur operateur = utilisateurRepository.findByLogin(loginOperateur)
                .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));
        Station station = resolveStation(operateur);
        Long compagnieId = station.getCompagnie().getId();

        Carte carte = carteRepository.findByNumeroCarteIgnoreCase(request.getNumeroCarte())
                .orElseThrow(() -> new EntityNotFoundException("Carte non trouvée"));

        if (carte.getStatut() != StatutCarte.ACTIVE) {
            throw new IllegalStateException("Cette carte n'est pas active");
        }

        Produit produit = null;
        ServiceCatalogue service = null;
        String nomArticle;

        if (request.getProduitId() != null) {
            produit = produitRepository.findByIdAndCompagnieId(request.getProduitId(), compagnieId)
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé pour cette compagnie"));
            nomArticle = produit.getNom();
        } else {
            service = serviceCatalogueRepository.findByIdAndCompagnieId(request.getServiceId(), compagnieId)
                    .orElseThrow(() -> new EntityNotFoundException("Service non trouvé pour cette compagnie"));
            if (service.getStatut() != StatutService.ACTIF) {
                throw new IllegalStateException("Ce service n'est pas actif");
            }
            if (!estDisponibleDansStation(service, station)) {
                throw new IllegalStateException("Ce service n'est pas disponible dans cette station");
            }
            nomArticle = service.getNom();
        }

        Transaction transaction = Transaction.builder()
                .montant(request.getMontant())
                .type(TypeTransaction.ACHAT)
                .statut(StatutTransaction.EN_ATTENTE)
                .produit(produit)
                .service(service)
                .station(station)
                .operateur(operateur)
                .client(carte.getEmploye())
                .reference(genererReference())
                .build();

        if (carte.getSolde().compareTo(request.getMontant()) < 0) {
            transaction.setStatut(StatutTransaction.ECHEC);
            transactionRepository.save(transaction);
            throw new IllegalStateException("Solde de la carte insuffisant");
        }

        carte.setSolde(carte.getSolde().subtract(request.getMontant()));
        carteRepository.save(carte);
        transaction.setStatut(StatutTransaction.REUSSIE);
        transaction = transactionRepository.save(transaction);

        creerNotification(carte.getEmploye(), "Paiement réussi",
                "Paiement de " + request.getMontant() + " FCFA effectué avec votre carte.",
                TypeNotificationPersonnelle.PAIEMENT_REUSSI, transaction);

        return AchatCarteResponse.builder()
                .transactionId(transaction.getId())
                .reference(transaction.getReference())
                .montant(request.getMontant())
                .produitNom(nomArticle)
                .employeNom(carte.getEmploye().getPrenom() + " " + carte.getEmploye().getNom())
                .carteMasquee(masquerCarte(carte.getNumeroCarte()))
                .nouveauSolde(carte.getSolde())
                .statut(transaction.getStatut().name())
                .stationNom(station.getNom())
                .dateTransaction(transaction.getDateTransaction())
                .build();
    }

  
    private StatsJourResponse calculerStats(Utilisateur operateur, Station station,
                                             LocalDateTime debut, LocalDateTime fin) {
        BigDecimal total;
        long totalCount;
        long successCount;

        if (operateur.getRole() == Role.POMPISTE) {
            total = transactionRepository.sumCaByStationAndOperateurAndPeriode(
                    station.getId(), operateur.getId(), debut, fin);
            totalCount = transactionRepository.countByStationIdAndOperateurIdAndTypeNotAndDateTransactionBetween(
                    station.getId(), operateur.getId(), TypeTransaction.RECHARGE, debut, fin);
            successCount = transactionRepository.countByStationIdAndOperateurIdAndTypeNotAndDateTransactionBetweenAndStatut(
                    station.getId(), operateur.getId(), TypeTransaction.RECHARGE, debut, fin, StatutTransaction.REUSSIE);
        } else {
            total = transactionRepository.sumCaByStationAndPeriode(station.getId(), debut, fin);
            totalCount = transactionRepository.countByStationIdAndDateTransactionBetween(station.getId(), debut, fin);
            successCount = transactionRepository.countByStationIdAndDateTransactionBetweenAndStatut(
                    station.getId(), debut, fin, StatutTransaction.REUSSIE);
        }

        return StatsJourResponse.builder()
                .totalAmount(total != null ? total : BigDecimal.ZERO)
                .totalCount(totalCount)
                .successCount(successCount)
                .build();
    }

    @Override
    public StatsJourResponse getStatsDuJour(String loginOperateur) {
        Utilisateur operateur = utilisateurRepository.findByLogin(loginOperateur)
                .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));
        Station station = resolveStation(operateur);

        LocalDateTime debut = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDateTime.now();

        return calculerStats(operateur, station, debut, fin);
    }

    @Override
    public List<TransactionRecenteResponse> getTransactionsRecentes(String loginOperateur) {
        Utilisateur operateur = utilisateurRepository.findByLogin(loginOperateur)
                .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));
        Station station = resolveStation(operateur);


        List<Transaction> transactions = operateur.getRole() == Role.POMPISTE
                ? transactionRepository.findTop10ByStationIdAndOperateurIdAndTypeNotOrderByDateTransactionDesc(
                        station.getId(), operateur.getId(), TypeTransaction.RECHARGE)
                : transactionRepository.findTop10ByStationIdOrderByDateTransactionDesc(station.getId());

        return transactions.stream().map(t -> TransactionRecenteResponse.builder()
                        .id(t.getId())
                        .reference(t.getReference())
                        .montant(t.getMontant())
                        .type(t.getType().name())
                        .statut(t.getStatut().name())
                        .produitOuServiceNom(t.getProduit() != null ? t.getProduit().getNom()
                                : (t.getService() != null ? t.getService().getNom() : null))
                        .clientNom(t.getClient() != null
                                ? t.getClient().getPrenom() + " " + t.getClient().getNom()
                                : null)
                        .operateurNom(t.getOperateur().getPrenom() + " " + t.getOperateur().getNom())
                        .dateTransaction(t.getDateTransaction())
                        .annulable(t.getStatut() == StatutTransaction.REUSSIE
                                && t.getDateTransaction().toLocalDate().isEqual(LocalDate.now()))
                        .build())
                .toList();
    }

    @Override
    public List<HistoriqueTransactionResponse> getTransactionsClient(String loginClient) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(loginClient)
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé"));

        List<Transaction> transactions =
                transactionRepository.findByClientIdOrderByDateTransactionDesc(utilisateur.getId());

        return transactions.stream().map(t -> HistoriqueTransactionResponse.builder()
                        .id(t.getId())
                        .reference(t.getReference())
                        .montant(t.getMontant())
                        .type(t.getType().name())
                        .statut(t.getStatut().name())
                        .produitOuServiceNom(t.getProduit() != null ? t.getProduit().getNom()
                                : (t.getService() != null ? t.getService().getNom() : null))
                        .stationNom(t.getStation().getNom())
                        .dateTransaction(t.getDateTransaction())
                        .pointsGagnes(t.getPointsGagnes())
                        .build())
                .toList();
    }


    @Override
    @Transactional(noRollbackFor = IllegalStateException.class)
    public ConfirmerTransactionResponse confirmerTransactionEmploye(String loginEmploye, ConfirmerTransactionRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(loginEmploye)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if (!(utilisateur instanceof Employe employe)) {
            throw new IllegalStateException("Cette action est réservée aux employés");
        }

        Carte carte = carteRepository.findByEmployeId(employe.getId())
                .orElseThrow(() -> new EntityNotFoundException("Aucune carte associée à cet employé"));

        if (carte.getStatut() != StatutCarte.ACTIVE) {
            throw new IllegalStateException("Cette carte n'est pas active");
        }

        Transaction transaction = transactionRepository.findByCodeConfirmation(request.getCode())
                .orElseThrow(() -> new EntityNotFoundException("Code invalide"));

        if (transaction.getStatut() != StatutTransaction.EN_ATTENTE) {
            throw new IllegalStateException("Cette transaction a déjà été traitée");
        }
        if (transaction.getCodeConfirmationExpiration().isBefore(LocalDateTime.now())) {
            transaction.setStatut(StatutTransaction.ECHEC);
            transactionRepository.save(transaction);
            throw new IllegalStateException("Code expiré, demandez à l'opérateur de réessayer");
        }

        BigDecimal montant = transaction.getMontant();

        if (carte.getSolde().compareTo(montant) < 0) {
            transaction.setStatut(StatutTransaction.ECHEC);
            transaction.setClient(employe);
            transactionRepository.save(transaction);
            throw new IllegalStateException("Solde insuffisant");
        }

        carte.setSolde(carte.getSolde().subtract(montant));
        carteRepository.save(carte);

        transaction.setClient(employe);
        transaction.setStatut(StatutTransaction.REUSSIE);
        transaction.setCodeConfirmation(null);
        transaction = transactionRepository.save(transaction);

        String nomArticle = transaction.getProduit() != null
                ? transaction.getProduit().getNom()
                : (transaction.getService() != null ? transaction.getService().getNom() : null);

        creerNotification(employe, "Paiement réussi",
                "Votre paiement de " + montant + " FCFA a été confirmé.",
                TypeNotificationPersonnelle.PAIEMENT_REUSSI, transaction);

        return ConfirmerTransactionResponse.builder()
                .transactionId(transaction.getId())
                .reference(transaction.getReference())
                .montant(montant)
                .produitNom(nomArticle)
                .stationNom(transaction.getStation().getNom())
                .nouveauSolde(carte.getSolde())
                .statut(transaction.getStatut().name())
                .dateTransaction(transaction.getDateTransaction())
                .pointsGagnes(0)
                .build();
    }

   
    @Override
    @Transactional(noRollbackFor = IllegalStateException.class)
    public PayerParQrResponse payerParQr(String loginOperateur, PayerParQrRequest request) {
        Utilisateur operateur = utilisateurRepository.findByLogin(loginOperateur)
                .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));
        Station station = resolveStation(operateur);
        Long compagnieId = station.getCompagnie().getId();

        Produit produit = null;
        ServiceCatalogue service = null;
        String nomArticle;

        if (request.getProduitId() != null) {
            produit = produitRepository.findByIdAndCompagnieId(request.getProduitId(), compagnieId)
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé pour cette compagnie"));
            nomArticle = produit.getNom();
        } else {
            service = serviceCatalogueRepository.findByIdAndCompagnieId(request.getServiceId(), compagnieId)
                    .orElseThrow(() -> new EntityNotFoundException("Service non trouvé pour cette compagnie"));
            if (service.getStatut() != StatutService.ACTIF) {
                throw new IllegalStateException("Ce service n'est pas actif");
            }
            if (!estDisponibleDansStation(service, station)) {
                throw new IllegalStateException("Ce service n'est pas disponible dans cette station");
            }
            nomArticle = service.getNom();
        }

     
        var clientOpt = clientRepository.findByQrCode(request.getCode());
        var carteOpt = carteRepository.findByCodeQr(request.getCode());

        if (clientOpt.isEmpty() && carteOpt.isEmpty()) {
            throw new EntityNotFoundException("QR code invalide ou expiré");
        }

        BigDecimal montant = request.getMontant();
        Transaction transaction;
        BigDecimal nouveauSolde;
        String porteurNom;
        Integer pointsGagnes = null;

        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            if (client.getQrCodeExpiration() == null || client.getQrCodeExpiration().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("QR code expiré, demandez au client de le régénérer");
            }

            transaction = Transaction.builder()
                    .montant(montant).type(TypeTransaction.ACHAT).statut(StatutTransaction.EN_ATTENTE)
                    .produit(produit).service(service).station(station).operateur(operateur)
                    .client(client).reference(genererReference())
                    .build();

            if (client.getSolde().compareTo(montant) < 0) {
                transaction.setStatut(StatutTransaction.ECHEC);
                transactionRepository.save(transaction);
                throw new IllegalStateException("Solde insuffisant");
            }

            client.setSolde(client.getSolde().subtract(montant));
            pointsGagnes = crediterPointsFidelite(client, station.getCompagnie(), montant);
            client.setQrCode(null);
            client.setQrCodeExpiration(null);
            clientRepository.save(client);

            transaction.setStatut(StatutTransaction.REUSSIE);
            transaction.setPointsGagnes(pointsGagnes);
            transaction = transactionRepository.save(transaction);

            nouveauSolde = client.getSolde();
            porteurNom = client.getPrenom() + " " + client.getNom();

            creerNotification(client, "Paiement réussi",
                    "Votre paiement de " + montant + " FCFA a été confirmé.",
                    TypeNotificationPersonnelle.PAIEMENT_REUSSI, transaction);

        } else {
            Carte carte = carteOpt.get();
            if (carte.getCodeQrExpiration() == null || carte.getCodeQrExpiration().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("QR code expiré, demandez à l'employé de le régénérer");
            }
            if (carte.getStatut() != StatutCarte.ACTIVE) {
                throw new IllegalStateException("Cette carte n'est pas active");
            }

            transaction = Transaction.builder()
                    .montant(montant).type(TypeTransaction.ACHAT).statut(StatutTransaction.EN_ATTENTE)
                    .produit(produit).service(service).station(station).operateur(operateur)
                    .client(carte.getEmploye()).reference(genererReference())
                    .build();

            if (carte.getSolde().compareTo(montant) < 0) {
                transaction.setStatut(StatutTransaction.ECHEC);
                transactionRepository.save(transaction);
                throw new IllegalStateException("Solde de la carte insuffisant");
            }

            carte.setSolde(carte.getSolde().subtract(montant));
            carte.setCodeQr(null);
            carte.setCodeQrExpiration(null);
            carteRepository.save(carte);

            transaction.setStatut(StatutTransaction.REUSSIE);
            transaction = transactionRepository.save(transaction);

            nouveauSolde = carte.getSolde();
            porteurNom = carte.getEmploye().getPrenom() + " " + carte.getEmploye().getNom();

            creerNotification(carte.getEmploye(), "Paiement réussi",
                    "Paiement de " + montant + " FCFA effectué avec votre carte.",
                    TypeNotificationPersonnelle.PAIEMENT_REUSSI, transaction);
        }

        return PayerParQrResponse.builder()
                .transactionId(transaction.getId())
                .reference(transaction.getReference())
                .montant(montant)
                .produitNom(nomArticle)
                .porteurNom(porteurNom)
                .stationNom(station.getNom())
                .nouveauSolde(nouveauSolde)
                .statut(transaction.getStatut().name())
                .dateTransaction(transaction.getDateTransaction())
                .pointsGagnes(pointsGagnes)
                .build();
    }


    @Override
    public ResoudreQrResponse resoudreQr(String code) {
        var clientOpt = clientRepository.findByQrCode(code);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            if (client.getQrCodeExpiration() == null || client.getQrCodeExpiration().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("QR code expiré, demandez au client de le régénérer");
            }
            return ResoudreQrResponse.builder()
                    .porteurNom(client.getPrenom() + " " + client.getNom())
                    .typePorteur("CLIENT")
                    .build();
        }

        var carteOpt = carteRepository.findByCodeQr(code);
        if (carteOpt.isPresent()) {
            Carte carte = carteOpt.get();
            if (carte.getCodeQrExpiration() == null || carte.getCodeQrExpiration().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("QR code expiré, demandez à l'employé de le régénérer");
            }
            return ResoudreQrResponse.builder()
                    .porteurNom(carte.getEmploye().getPrenom() + " " + carte.getEmploye().getNom())
                    .typePorteur("EMPLOYE")
                    .build();
        }

        throw new EntityNotFoundException("QR code invalide ou expiré");
    }

    @Override
@Transactional(noRollbackFor = IllegalStateException.class)
public AnnulerTransactionResponse annulerTransaction(String loginOperateur, Long transactionId, AnnulerTransactionRequest request) {
    Utilisateur operateur = utilisateurRepository.findByLogin(loginOperateur)
            .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));
    Station stationOperateur = resolveStation(operateur);

    Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new EntityNotFoundException("Transaction non trouvée"));

    if (!transaction.getStation().getId().equals(stationOperateur.getId())) {
        throw new IllegalStateException("Cette transaction n'appartient pas à votre station");
    }
    if (transaction.getStatut() != StatutTransaction.REUSSIE) {
        throw new IllegalStateException("Seule une transaction réussie peut être annulée");
    }
    if (!transaction.getDateTransaction().toLocalDate().isEqual(LocalDate.now())) {
        throw new IllegalStateException("Seules les transactions du jour peuvent être annulées");
    }

    BigDecimal montant = transaction.getMontant();


    Long clientOuEmployeId = transaction.getClient() != null ? transaction.getClient().getId() : null;

    if (clientOuEmployeId != null) {
        if (transaction.getType() == TypeTransaction.RECHARGE) {
            Client c = clientRepository.findById(clientOuEmployeId)
                    .orElseThrow(() -> new IllegalStateException("Client introuvable pour cette recharge"));
            if (c.getSolde().compareTo(montant) < 0) {
                throw new IllegalStateException(
                        "Impossible d'annuler : le client a déjà utilisé une partie de cette recharge");
            }
            c.setSolde(c.getSolde().subtract(montant));
            clientRepository.save(c);
        } else {
            // ACHAT : soit un Client particulier (Vente), soit un Employé (Achat carte)
            var clientOpt = clientRepository.findById(clientOuEmployeId);
            if (clientOpt.isPresent()) {
                Client c = clientOpt.get();
                c.setSolde(c.getSolde().add(montant));
                clientRepository.save(c);

                if (transaction.getPointsGagnes() != null && transaction.getPointsGagnes() > 0) {
                    retirerPointsFidelite(c, transaction.getStation().getCompagnie(), transaction.getPointsGagnes());
                }
            } else {
                Carte carte = carteRepository.findByEmployeId(clientOuEmployeId)
                        .orElseThrow(() -> new IllegalStateException("Carte introuvable pour cet employé"));
                carte.setSolde(carte.getSolde().add(montant));
                carteRepository.save(carte);
            }
        }
    }

    transaction.setStatut(StatutTransaction.ANNULEE);
    transaction.setDateAnnulation(LocalDateTime.now());
    transaction.setAnnulePar(operateur);
    transaction.setMotifAnnulation(request.getMotif());
    transaction = transactionRepository.save(transaction);

    return AnnulerTransactionResponse.builder()
            .transactionId(transaction.getId())
            .reference(transaction.getReference())
            .montant(montant)
            .statut(transaction.getStatut().name())
            .dateAnnulation(transaction.getDateAnnulation())
            .motif(request.getMotif())
            .build();
}

    private void creerNotification(Utilisateur destinataire, String titre, String message,
                                     TypeNotificationPersonnelle type, Transaction transaction) {
        notificationRepository.save(NotificationPersonnelle.builder()
                .destinataire(destinataire)
                .titre(titre)
                .message(message)
                .type(type)
                .transaction(transaction)
                .build());
    }

    private String masquerTelephone(String telephone) {
        if (telephone == null || telephone.length() < 4) return telephone;
        return telephone.substring(0, 2) + "*****" + telephone.substring(telephone.length() - 2);
    }

    private String masquerCarte(String numeroCarte) {
        if (numeroCarte == null || numeroCarte.length() < 4) return numeroCarte;
        String derniers4 = numeroCarte.substring(numeroCarte.length() - 4);
        return "*".repeat(numeroCarte.length() - 4) + derniers4;
    }

    @Override
    public StatutTransactionResponse getStatut(String loginOperateur, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction non trouvée"));

        if (!transaction.getOperateur().getLogin().equals(loginOperateur)) {
            throw new IllegalStateException("Accès non autorisé à cette transaction");
        }

       
return StatutTransactionResponse.builder()
        .transactionId(transaction.getId())
        .statut(transaction.getStatut().name())
        .reference(transaction.getReference())
        .clientNom(transaction.getClient() != null
                ? transaction.getClient().getPrenom() + " " + transaction.getClient().getNom()
                : null)
        .build();
    }

    private String genererCodeUnique() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(20);
            for (int i = 0; i < 20; i++) {
                sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
            code = sb.toString();
        } while (transactionRepository.findByCodeConfirmation(code).isPresent());
        return code;
    }

    private String genererReference() {
        String horodatageCompact = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        String suffixe = String.format("%02d", random.nextInt(100));
        return "TXN" + horodatageCompact + suffixe;
    }

    @Override
    public StatsJourResponse getStatsPeriode(String loginOperateur, LocalDate debut, LocalDate fin) {
        Utilisateur operateur = utilisateurRepository.findByLogin(loginOperateur)
                .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));
        Station station = resolveStation(operateur);

        LocalDateTime debutDt = debut.atStartOfDay();
        LocalDateTime finDt = fin.atTime(23, 59, 59);

        return calculerStats(operateur, station, debutDt, finDt);
    }


@Override
@Transactional(noRollbackFor = IllegalStateException.class)
public RechargeClientResponse rechargerParQr(String loginOperateur, RechargeParQrRequest request) {
    Utilisateur operateur = utilisateurRepository.findByLogin(loginOperateur)
            .orElseThrow(() -> new EntityNotFoundException("Opérateur non trouvé"));
    Station station = resolveStation(operateur);

    var clientOpt = clientRepository.findByQrCode(request.getCode());

    if (clientOpt.isEmpty()) {
        if (carteRepository.findByCodeQr(request.getCode()).isPresent()) {
            throw new IllegalStateException(
                    "Ce QR appartient à un employé — la recharge d'un employé se fait via son entreprise, pas en station");
        }
        throw new EntityNotFoundException("QR code invalide ou expiré");
    }

    Client client = clientOpt.get();
    if (client.getQrCodeExpiration() == null || client.getQrCodeExpiration().isBefore(LocalDateTime.now())) {
        throw new IllegalStateException("QR code expiré, demandez au client de le régénérer");
    }

    Transaction transaction = Transaction.builder()
            .montant(request.getMontant())
            .type(TypeTransaction.RECHARGE)
            .statut(StatutTransaction.REUSSIE)
            .station(station)
            .operateur(operateur)
            .client(client)
            .reference(genererReference())
            .build();
    transaction = transactionRepository.save(transaction);

    client.setSolde(client.getSolde().add(request.getMontant()));

    client.setQrCode(null);
    client.setQrCodeExpiration(null);
    clientRepository.save(client);

    creerNotification(client, "Recharge reçue",
            "Votre compte a été crédité de " + request.getMontant() + " FCFA.",
            TypeNotificationPersonnelle.RECHARGE_REUSSIE, transaction);

    return RechargeClientResponse.builder()
            .transactionId(transaction.getId())
            .reference(transaction.getReference())
            .montant(request.getMontant())
            .clientNom(client.getPrenom() + " " + client.getNom())
            .telephoneMasque(masquerTelephone(client.getTelephone()))
            .nouveauSolde(client.getSolde())
            .statut(transaction.getStatut().name())
            .stationNom(station.getNom())
            .dateTransaction(transaction.getDateTransaction())
            .build();
}


}