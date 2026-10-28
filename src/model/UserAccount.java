package model;

public class UserAccount {

    private final long userId;
    private final String username;
    private final String email;
    private final String passwordHash;

    public UserAccount(long userId, String username, String email,
                       String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
