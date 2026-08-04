package cardoil.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void envoyerCredentials(String destinataire, String prenom, String nom,
                                    String login, String motDePasse, String nomStation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("contacts@cardoilbo-test.com");
        message.setTo(destinataire);
        message.setSubject("Cardoil — Vos identifiants de connexion");
        message.setText(
            "Bonjour " + prenom + " " + nom + ",\n\n" +
            "Un compte Gérant a été créé pour vous sur la plateforme Cardoil.\n\n" +
            (nomStation != null ? "Station assignée : " + nomStation + "\n\n" : "") +
            "Vos identifiants de connexion :\n" +
            "  Login       : " + login + "\n" +
            "  Mot de passe : " + motDePasse + "\n\n" +
            "⚠️ Vous devrez changer votre mot de passe à la première connexion.\n\n" +
            "Accédez à la plateforme sur : https://test.cardoil.io/login\n\n" +
            "Cordialement,\n" +
            "L'équipe Cardoil"
        );
        mailSender.send(message);
    }

    // À ajouter dans EmailService.java, à côté de envoyerCredentials(...)

    public void envoyerCredentialsEmploye(String destinataire, String prenom, String nom,
                                           String login, String motDePasse,
                                           String nomEntreprise, String nomDepartement) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("contacts@cardoilbo-test.com");
        message.setTo(destinataire);
        message.setSubject("Cardoil — Vos identifiants de connexion");
        message.setText(
            "Bonjour " + prenom + " " + nom + ",\n\n" +
            "Un compte Employé a été créé pour vous sur la plateforme Cardoil.\n\n" +
            "Entreprise : " + nomEntreprise + "\n" +
            (nomDepartement != null ? "Département : " + nomDepartement + "\n" : "") +
            "\n" +
            "Vos identifiants de connexion :\n" +
            "  Login       : " + login + "\n" +
            "  Mot de passe : " + motDePasse + "\n\n" +
            "⚠️ Vous devrez changer votre mot de passe à la première connexion.\n\n" +
            "Accédez à la plateforme sur : https://test.cardoil.io/login\n\n" +
            "Cordialement,\n" +
            "L'équipe Cardoil"
        );
        mailSender.send(message);
    }

    // À ajouter dans EmailService.java, à côté des autres méthodes envoyerCredentials...

    public void envoyerCredentialsAdminDepartement(String destinataire, String prenom, String nom,
                                                     String login, String motDePasse,
                                                     String nomEntreprise, String nomDepartement) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("contacts@cardoilbo-test.com");
        message.setTo(destinataire);
        message.setSubject("Cardoil — Vos identifiants de connexion");
        message.setText(
            "Bonjour " + prenom + " " + nom + ",\n\n" +
            "Un compte Administrateur de Département a été créé pour vous sur la plateforme Cardoil.\n\n" +
            "Entreprise : " + nomEntreprise + "\n" +
            "Département : " + nomDepartement + "\n\n" +
            "Vos identifiants de connexion :\n" +
            "  Login       : " + login + "\n" +
            "  Mot de passe : " + motDePasse + "\n\n" +
            "⚠️ Vous devrez changer votre mot de passe à la première connexion.\n\n" +
            "Accédez à la plateforme sur : https://test.cardoil.io/login\n\n" +
            "Cordialement,\n" +
            "L'équipe Cardoil"
        );
        mailSender.send(message);
    }
}