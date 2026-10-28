package model;

public class User {

    private String username;
    private String password;
    private String email;

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Basic validation
    public boolean isValid() {
        return username != null && !username.isBlank()
                && password != null && !password.isBlank()
                && email != null && !email.isBlank();
    }
}