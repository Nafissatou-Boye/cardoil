package cardoil.backend.service.impl;

import cardoil.backend.dto.request.TransactionRequest;
import cardoil.backend.dto.response.ProduitResponse;
import cardoil.backend.dto.response.TransactionResponse;
import cardoil.backend.entity.*;
import cardoil.backend.repository.*;
import cardoil.backend.service.FideliteService;
import cardoil.backend.service.GerantTransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GerantTransactionServiceImpl implements GerantTransactionService {

    private final UtilisateurRepository utilisateurRepository;
    private final StationRepository stationRepository;
    private final TransactionRepository transactionRepository;
    private final ProduitRepository produitRepository;
    private final PrixJourRepository prixJourRepository;
    private final FideliteService fideliteService;
    private final PromotionRepository promotionRepository;
    private final CarteRepository carteRepository;
    private final ClientRepository clientRepository;
    private final RechargeRepository rechargeRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String CHARS_REFERENCE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom random = new SecureRandom();

    // Petit conteneur interne pour porter le résultat de l'identification du client
    // (soit une Carte d'Employé, soit un Client particulier — jamais les deux)
    private record ClientResolu(Carte carte, Client client, Utilisateur utilisateur,
                                 String nomComplet, String identifiantMasque, String typeClient) {
    }

    @Override
    public TransactionResponse create(String login, TransactionRequest request) {
        Utilisateur gerant = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        Station station = getStation(gerant);

        ClientResolu clientResolu = resoudreClient(request);

        Produit produitFinal = null;
        BigDecimal prixTtc = null;
        BigDecimal prixHtva = null;
        BigDecimal prixHtt = null;

        if (request.getType() == TypeTransaction.ACHAT) {
            if (request.getProduitId() == null) {
                throw new IllegalArgumentException("Le produit est obligatoire pour un achat");
            }

            Produit p = produitRepository.findByIdAndCompagnieId(
                            request.getProduitId(), station.getCompagnie().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé"));

            if (p.getStatut() != StatutProduit.ACTIF) {
                throw new IllegalStateException("Ce produit n'est pas actif");
            }

            Optional<PrixJour> prixDuJour = prixJourRepository
                    .findByProduitIdAndDatePrix(p.getId(), LocalDate.now());

            PrixJour prix = prixDuJour.orElseGet(() ->
                    prixJourRepository.findByProduitIdOrderByDatePrixDesc(p.getId())
                            .stream().findFirst().orElse(null));

            if (prix != null) {
                prixTtc = prix.getPrixTtc();
                prixHtva = prix.getPrixHtva();
                prixHtt = prix.getPrixHtt();
            }

            produitFinal = p;
        }

        // ===== Mouvement de solde réel =====
        appliquerMouvementSolde(request, clientResolu, gerant);

        Transaction transaction = Transaction.builder()
                .reference(genererReferenceUnique())
                .montant(request.getMontant())
                .type(request.getType())
                .statut(StatutTransaction.REUSSIE)
                .prixTtc(prixTtc)
                .prixHtva(prixHtva)
                .prixHtt(prixHtt)
                .produit(produitFinal)
                .station(station)
                .operateur(gerant)
                .client(clientResolu.utilisateur())
                .build();

        transaction = transactionRepository.save(transaction);

        // QR code à usage unique : invalidé dès qu'il a servi à une transaction réussie,
        // même s'il n'a pas encore atteint ses 15 minutes de validité.
        if (request.getQrCode() != null && !request.getQrCode().isBlank() && clientResolu.client() != null) {
            Client clientQr = clientResolu.client();
            clientQr.setQrCode(null);
            clientQr.setQrCodeExpiration(null);
            clientRepository.save(clientQr);
        }

        // Attribution automatique des points si promotion POINTS active
        final Transaction savedTransaction = transaction;
        final Station savedStation = station;
        promotionRepository.findByCompagnieIdOrderByDateDebutDesc(station.getCompagnie().getId())
                .stream()
                .filter(p -> p.getStatut() == StatutPromotion.ACTIVE
                        && p.getType() == TypePromotion.POINTS
                        && !p.getDateFin().isBefore(LocalDateTime.now())
                        && (p.getStationsConcernees() == null
                            || p.getStationsConcernees().isEmpty()
                            || p.getStationsConcernees().stream()
                                .anyMatch(s -> s.getId().equals(savedStation.getId()))))
                .findFirst()
                .ifPresent(promo -> fideliteService.attribuerPoints(savedTransaction, promo));

        return toResponse(transaction, clientResolu);
    }

    @Override
    public List<TransactionResponse> getRecentes(String login) {
        Utilisateur gerant = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        Station station = getStation(gerant);

        return transactionRepository
                .findTop10ByStationIdOrderByDateTransactionDesc(station.getId())
                .stream().map(t -> toResponse(t, null)).toList();
    }

    @Override
    public List<ProduitResponse> getProduitsDisponibles(String login) {
        Utilisateur gerant = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        Station station = getStation(gerant);

        return produitRepository
                .findByCompagnieIdAndStatut(station.getCompagnie().getId(), StatutProduit.ACTIF)
                .stream().map(this::toProduitResponse).toList();
    }

    // ===== RÉSOLUTION DU CLIENT (carte, téléphone, ou QR code rotatif) =====

    private ClientResolu resoudreClient(TransactionRequest request) {
        boolean aCarte = request.getNumeroCarte() != null && !request.getNumeroCarte().isBlank();
        boolean aTelephone = request.getTelephoneClient() != null && !request.getTelephoneClient().isBlank();
        boolean aQrCode = request.getQrCode() != null && !request.getQrCode().isBlank();

        int nombreIdentifiants = (aCarte ? 1 : 0) + (aTelephone ? 1 : 0) + (aQrCode ? 1 : 0);
        if (nombreIdentifiants != 1) {
            throw new IllegalArgumentException(
                    "Vous devez identifier le client par carte (Employé), téléphone, ou QR code (Client particulier) — un seul moyen à la fois");
        }

        if (aCarte) {
            Carte carte = carteRepository.findByNumeroCarteIgnoreCase(request.getNumeroCarte().trim())
                    .orElseThrow(() -> new EntityNotFoundException("Carte introuvable"));

            Employe employe = carte.getEmploye();
            return new ClientResolu(
                    carte, null, employe,
                    employe.getPrenom() + " " + employe.getNom(),
                    masquerCarte(carte.getNumeroCarte()),
                    "EMPLOYE"
            );
        }

        if (aQrCode) {
            Client client = clientRepository.findByQrCode(request.getQrCode().trim())
                    .orElseThrow(() -> new EntityNotFoundException("QR code invalide ou expiré"));

            if (client.getQrCodeExpiration() == null || LocalDateTime.now().isAfter(client.getQrCodeExpiration())) {
                throw new IllegalStateException("QR code expiré, demandez au client d'en générer un nouveau");
            }

            if (!client.isTelephoneVerifie()) {
                throw new IllegalStateException("Le numéro de ce client n'est pas encore vérifié");
            }

            return new ClientResolu(
                    null, client, client,
                    client.getPrenom() != null ? client.getPrenom() + " " + client.getNom() : "Client",
                    masquerTelephone(client.getTelephone()),
                    "CLIENT_PARTICULIER"
            );
        }

        // aTelephone : identification manuelle, sans QR (secours si le client ne peut pas scanner)
        Client client = clientRepository.findByTelephone(request.getTelephoneClient().trim())
                .orElseThrow(() -> new EntityNotFoundException("Aucun client trouvé pour ce numéro"));

        if (!client.isTelephoneVerifie()) {
            throw new IllegalStateException("Le numéro de ce client n'est pas encore vérifié");
        }

        return new ClientResolu(
                null, client, client,
                client.getPrenom() != null ? client.getPrenom() + " " + client.getNom() : "Client",
                masquerTelephone(client.getTelephone()),
                "CLIENT_PARTICULIER"
        );
    }

    // ===== MOUVEMENT DE SOLDE RÉEL =====

    private void appliquerMouvementSolde(TransactionRequest request, ClientResolu clientResolu, Utilisateur gerant) {
        BigDecimal montant = request.getMontant();

        if (clientResolu.carte() != null) {
            Carte carte = clientResolu.carte();

            if (carte.getStatut() != StatutCarte.ACTIVE) {
                throw new IllegalStateException("Carte " + carte.getStatut() + " : transaction impossible");
            }

            if (request.getType() == TypeTransaction.RECHARGE) {
                // Recharge cash à la pompe : argent personnel remis au gérant,
                // ne touche pas le budget département/entreprise (différent d'une recharge Admin).
                carte.setSolde(carte.getSolde().add(montant));
                carteRepository.save(carte);

                Recharge recharge = Recharge.builder()
                        .carte(carte)
                        .montant(montant)
                        .effectuePar(gerant)
                        .type(TypeRecharge.MANUELLE)
                        .build();
                rechargeRepository.save(recharge);
            } else {
                if (carte.getSolde().compareTo(montant) < 0) {
                    throw new IllegalArgumentException(
                            "Solde insuffisant : disponible " + carte.getSolde() + " FCFA, demandé " + montant + " FCFA");
                }
                carte.setSolde(carte.getSolde().subtract(montant));
                carteRepository.save(carte);
            }

        } else {
            Client client = clientResolu.client();

            if (!client.isActif()) {
                throw new IllegalStateException("Ce compte client est désactivé");
            }

            if (request.getType() == TypeTransaction.RECHARGE) {
                client.setSolde(client.getSolde().add(montant));
                clientRepository.save(client);
            } else {
                if (client.getSolde().compareTo(montant) < 0) {
                    throw new IllegalArgumentException(
                            "Solde insuffisant : disponible " + client.getSolde() + " FCFA, demandé " + montant + " FCFA");
                }
                client.setSolde(client.getSolde().subtract(montant));
                clientRepository.save(client);
            }
        }
    }

    // ===== HELPERS =====

    private Station getStation(Utilisateur gerant) {
        return stationRepository.findByGerantId(gerant.getId())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucune station assignée à ce gérant"));
    }

    private String genererReferenceUnique() {
        String reference;
        do {
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 12; i++) {
                sb.append(CHARS_REFERENCE.charAt(random.nextInt(CHARS_REFERENCE.length())));
            }
            reference = sb.toString();
        } while (transactionRepository.existsByReference(reference));
        return reference;
    }

    private String masquerTelephone(String telephone) {
        if (telephone == null || telephone.length() < 5) return telephone;
        String debut = telephone.substring(0, 2);
        String fin = telephone.substring(telephone.length() - 2);
        return debut + "*".repeat(telephone.length() - 4) + fin;
    }

    private String masquerCarte(String numeroCarte) {
        if (numeroCarte == null || numeroCarte.length() < 4) return numeroCarte;
        String fin = numeroCarte.substring(numeroCarte.length() - 4);
        return "*".repeat(12) + fin;
    }

    private TransactionResponse toResponse(Transaction t, ClientResolu clientResolu) {
        TransactionResponse.TransactionResponseBuilder builder = TransactionResponse.builder()
                .id(t.getId())
                .reference(t.getReference())
                .dateTransaction(t.getDateTransaction().format(FORMATTER))
                .montant(t.getMontant())
                .type(t.getType().name())
                .statut(t.getStatut().name())
                .produitNom(t.getProduit() != null ? t.getProduit().getNom() : null)
                .prixTtc(t.getPrixTtc());

        if (clientResolu != null) {
            builder.clientNomComplet(clientResolu.nomComplet())
                    .clientIdentifiantMasque(clientResolu.identifiantMasque())
                    .clientType(clientResolu.typeClient());
        } else if (t.getClient() != null) {
            // Cas getRecentes() : on ne recalcule pas la résolution complète,
            // on affiche juste le nom pour l'historique.
            builder.clientNomComplet(t.getClient().getPrenom() + " " + t.getClient().getNom());
        }

        return builder.build();
    }

    private ProduitResponse toProduitResponse(Produit produit) {
        Optional<PrixJour> dernierPrix = prixJourRepository
                .findByProduitIdOrderByDatePrixDesc(produit.getId())
                .stream().findFirst();

        ProduitResponse.ProduitResponseBuilder builder = ProduitResponse.builder()
                .id(produit.getId())
                .nom(produit.getNom())
                .type(produit.getType().name())
                .statut(produit.getStatut().name())
                .description(produit.getDescription())
                .obligatoire(produit.isObligatoire());

        dernierPrix.ifPresent(p -> builder
                .prixTtcActuel(p.getPrixTtc())
                .prixHtvaActuel(p.getPrixHtva())
                .prixHttActuel(p.getPrixHtt())
                .datePrixActuel(p.getDatePrix().toString()));

        return builder.build();
    }
}