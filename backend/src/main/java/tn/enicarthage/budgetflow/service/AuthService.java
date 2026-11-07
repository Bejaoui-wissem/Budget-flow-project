package tn.enicarthage.budgetflow.service;

import tn.enicarthage.budgetflow.dao.UserDAO;
import tn.enicarthage.budgetflow.dto.AuthResponse;
import tn.enicarthage.budgetflow.dto.LoginRequest;
import tn.enicarthage.budgetflow.dto.RegisterRequest;
import tn.enicarthage.budgetflow.dto.UserView;
import tn.enicarthage.budgetflow.exception.ApiException;
import tn.enicarthage.budgetflow.model.User;
import tn.enicarthage.budgetflow.util.JwtUtil;
import tn.enicarthage.budgetflow.util.PasswordUtil;
import tn.enicarthage.budgetflow.util.Validator;

import java.sql.SQLException;

/** Inscription, connexion et identification de l'utilisateur courant. */
public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public AuthResponse register(RegisterRequest r) throws SQLException {
        String nom = Validator.required(r.nom(), "Nom", 50);
        String prenom = Validator.required(r.prenom(), "Prénom", 50);
        String email = Validator.email(r.email());
        String password = r.motDePasse();
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw ApiException.badRequest("Le mot de passe doit contenir au moins "
                    + MIN_PASSWORD_LENGTH + " caractères");
        }
        if (password.length() > 72) {
            throw ApiException.badRequest("Mot de passe trop long (72 caractères maximum)");
        }
        if (!password.equals(r.confirmationMotDePasse())) {
            throw ApiException.badRequest("Les mots de passe ne correspondent pas");
        }
        if (userDAO.findByEmail(email).isPresent()) {
            throw ApiException.conflict("Un compte existe déjà avec cet e-mail");
        }
        int id = userDAO.insert(nom, prenom, email, PasswordUtil.hash(password));
        return new AuthResponse(JwtUtil.generate(id, email), new UserView(id, nom, prenom, email));
    }

    public AuthResponse login(LoginRequest r) throws SQLException {
        if (r.email() == null || r.motDePasse() == null) {
            throw ApiException.badRequest("E-mail et mot de passe obligatoires");
        }
        User user = userDAO.findByEmail(r.email().trim().toLowerCase()).orElse(null);
        if (user == null || !PasswordUtil.matches(r.motDePasse(), user.motDePasse())) {
            throw ApiException.unauthorized("E-mail ou mot de passe incorrect");
        }
        return new AuthResponse(JwtUtil.generate(user.id(), user.email()),
                new UserView(user.id(), user.nom(), user.prenom(), user.email()));
    }

    public UserView me(int userId) throws SQLException {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Utilisateur introuvable"));
        return new UserView(user.id(), user.nom(), user.prenom(), user.email());
    }
}
