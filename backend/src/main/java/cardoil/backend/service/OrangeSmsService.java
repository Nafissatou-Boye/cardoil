package cardoil.backend.service;

import cardoil.backend.config.OrangeSmsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Port fidèle de l'implémentation déjà fonctionnelle du projet CardOilJava —
// même algorithme (GET signé HMAC-SHA1 + Basic Auth), pas une réécriture.
@Slf4j
@Service
@RequiredArgsConstructor
public class OrangeSmsService {

    private final OrangeSmsProperties proprietes;

    public boolean envoyerSms(String destinataire, String contenu) {
        try {
            String destinataireFormate = formaterNumero(destinataire);

            log.info("Envoi SMS à {}", destinataireFormate);

            String contenuEncode = URLEncoder.encode(contenu, StandardCharsets.UTF_8);
            String sujetEncode = URLEncoder.encode(proprietes.getSubject(), StandardCharsets.UTF_8);
            String signatureEncodee = URLEncoder.encode(proprietes.getSignature(), StandardCharsets.UTF_8);

            long timestamp = System.currentTimeMillis() / 1000;

            // Important : le message à signer utilise les valeurs NON encodées,
            // contrairement à celles insérées dans l'URL finale.
            String messageAChiffrer = proprietes.getToken() + proprietes.getSubject() + proprietes.getSignature()
                    + destinataireFormate + contenu + timestamp;
            String cle = hmacSha1(proprietes.getPrivateKey(), messageAChiffrer);

            String url = proprietes.getApiUrl() + "?token=" + proprietes.getToken()
                    + "&subject=" + sujetEncode
                    + "&signature=" + signatureEncodee
                    + "&recipient=" + destinataireFormate
                    + "&content=" + contenuEncode
                    + "&timestamp=" + timestamp
                    + "&key=" + cle;

            HttpsURLConnection connexion = (HttpsURLConnection) new URL(url).openConnection();

            String authString = proprietes.getLogin() + ":" + proprietes.getToken();
            String authStringEncodee = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
            connexion.setRequestProperty("Authorization", "Basic " + authStringEncodee);

            int codeReponse = connexion.getResponseCode();

            if (codeReponse == 401) {
                log.error("Authentification Orange SMS échouée : login ou token incorrect");
                return false;
            }

            BufferedReader lecteur = new BufferedReader(new InputStreamReader(
                    (codeReponse >= 200 && codeReponse < 300) ? connexion.getInputStream() : connexion.getErrorStream()));

            StringBuilder reponse = new StringBuilder();
            String ligne;
            while ((ligne = lecteur.readLine()) != null) {
                reponse.append(ligne);
            }
            lecteur.close();

            String reponseTexte = reponse.toString();
            log.info("Réponse Orange SMS (code {}) : {}", codeReponse, reponseTexte);

            if (reponseTexte.contains("STATUS_CODE: 0")) {
                log.info("SMS envoyé avec succès");
                return true;
            } else if (reponseTexte.contains("STATUS_CODE: 116")) {
                log.error("Token Orange SMS invalide");
                return false;
            }

            // ⚠️ Dans la version d'origine (déjà en prod ailleurs), "STATUS_CODE:200" renvoie false —
            // incohérence jamais élucidée là-bas non plus. Reproduite à l'identique plutôt que
            // "corrigée" sans certitude sur le vrai comportement voulu de l'API.
            return codeReponse == 200 && !reponseTexte.contains("STATUS_CODE:200");

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du SMS à {}", destinataire, e);
            return false;
        }
    }

    public boolean envoyerCredentials(String telephone, String login, String motDePasse) {
        String message = "Bienvenue sur CardOil!\n" +
                "Vos identifiants de connexion:\n" +
                "Login: " + login + "\n" +
                "Mot de passe: " + motDePasse + "\n" +
                "Connectez-vous sur notre application.";
        return envoyerSms(telephone, message);
    }

    public boolean envoyerCodeOtp(String telephone, String code) {
        String message = "CardOil - Code de validation\n" +
                "Votre code de validation est: " + code + "\n" +
                "Ce code expire dans 10 minutes.";
        return envoyerSms(telephone, message);
    }

    private String formaterNumero(String telephone) {
        if (telephone == null) return "";
        String formate = telephone.startsWith("+") ? telephone.substring(1) : telephone;
        return formate.replaceAll("[\\s-]", "");
    }

    private String hmacSha1(String cleSecrete, String valeur) {
        try {
            SecretKeySpec cleSignature = new SecretKeySpec(cleSecrete.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(cleSignature);
            byte[] rawHmac = mac.doFinal(valeur.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(rawHmac.length * 2);
            for (byte b : rawHmac) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (Exception ex) {
            throw new RuntimeException("Erreur lors du calcul HMAC", ex);
        }
    }
}