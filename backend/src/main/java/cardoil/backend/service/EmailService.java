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
            "Accédez à la plateforme sur : http://localhost:4200/login\n\n" +
            "Cordialement,\n" +
            "L'équipe Cardoil"
        );
        mailSender.send(message);
    }
}