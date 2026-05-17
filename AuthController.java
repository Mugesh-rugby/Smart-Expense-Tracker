package controller;

import database.UserDAO;
import model.User;
import utils.PasswordUtil;
import utils.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * AuthController - Handles authentication business logic (MVC Controller layer).
 */
public class AuthController {

    private UserDAO userDAO;

    public AuthController() throws SQLException {
        this.userDAO = new UserDAO();
    }

    /** Authenticates user. Returns User on success, throws on failure. */
    public User login(String username, String plainPassword) {
        if (!ValidationUtil.isNotBlank(username))
            throw new IllegalArgumentException("Username is required.");
        if (!ValidationUtil.isNotBlank(plainPassword))
            throw new IllegalArgumentException("Password is required.");
        try {
            Optional<User> user = userDAO.authenticate(username.trim(), plainPassword);
            return user.orElseThrow(
                () -> new RuntimeException("Invalid username or password. Please try again."));
        } catch (SQLException e) {
            throw new RuntimeException("Database error during login: " + e.getMessage(), e);
        }
    }

    /** Registers a new user. Returns new user ID. */
    public int register(String fullName, String username, String email,
                        String plainPassword, String confirmPassword) {
        if (!ValidationUtil.isValidName(fullName))
            throw new IllegalArgumentException("Full name must be 2-100 characters.");
        if (!ValidationUtil.isValidUsername(username))
            throw new IllegalArgumentException("Username: 3-30 chars, letters/digits/underscore only.");
        if (!ValidationUtil.isValidEmail(email))
            throw new IllegalArgumentException("Please enter a valid email address.");
        if (!plainPassword.equals(confirmPassword))
            throw new IllegalArgumentException("Passwords do not match.");
        if (!PasswordUtil.isStrongPassword(plainPassword))
            throw new IllegalArgumentException(PasswordUtil.getPasswordStrengthMessage(plainPassword));
        try {
            if (userDAO.usernameExists(username.trim()))
                throw new IllegalArgumentException("Username '" + username + "' is already taken.");
            if (userDAO.emailExists(email.trim()))
                throw new IllegalArgumentException("An account with this email already exists.");
            User newUser = new User(username.trim().toLowerCase(), email.trim().toLowerCase(), "", fullName.trim());
            return userDAO.registerUser(newUser, plainPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    /** Resets password for a given email. */
    public void resetPassword(String email, String newPassword, String confirmPassword) {
        if (!ValidationUtil.isValidEmail(email))
            throw new IllegalArgumentException("Enter a valid email address.");
        if (!newPassword.equals(confirmPassword))
            throw new IllegalArgumentException("Passwords do not match.");
        if (!PasswordUtil.isStrongPassword(newPassword))
            throw new IllegalArgumentException(PasswordUtil.getPasswordStrengthMessage(newPassword));
        try {
            Optional<User> user = userDAO.findByEmail(email.trim());
            if (user.isEmpty())
                throw new IllegalArgumentException("No account found with this email.");
            userDAO.updatePassword(user.get().getId(), newPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }
}
