package model;

import java.time.LocalDateTime;

/**
 * User - Entity class representing an application user.
 * Demonstrates Encapsulation via private fields + getters/setters.
 */
public class User {

    private int           id;
    private String        username;
    private String        email;
    private String        passwordHash;
    private String        fullName;
    private LocalDateTime createdAt;
    private boolean       active;

    // ── Constructors ───────────────────────────────────────────────────────

    public User() {}

    public User(String username, String email, String passwordHash, String fullName) {
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.active       = true;
    }

    public User(int id, String username, String email,
                String passwordHash, String fullName,
                LocalDateTime createdAt, boolean active) {
        this.id           = id;
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.createdAt    = createdAt;
        this.active       = active;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public int           getId()           { return id; }
    public void          setId(int id)     { this.id = id; }

    public String        getUsername()                   { return username; }
    public void          setUsername(String username)    { this.username = username; }

    public String        getEmail()                      { return email; }
    public void          setEmail(String email)          { this.email = email; }

    public String        getPasswordHash()                          { return passwordHash; }
    public void          setPasswordHash(String passwordHash)       { this.passwordHash = passwordHash; }

    public String        getFullName()                  { return fullName; }
    public void          setFullName(String fullName)   { this.fullName = fullName; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    public boolean       isActive()                     { return active; }
    public void          setActive(boolean active)      { this.active = active; }

    /** Returns user's first name for friendly greeting. */
    public String getFirstName() {
        if (fullName == null || fullName.isEmpty()) return username;
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
