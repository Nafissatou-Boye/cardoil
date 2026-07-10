package cardoil.backend.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String login;
    private String motDePasse;
    private String role;
    private String nom;
    private String prenom;
    private String email;
}