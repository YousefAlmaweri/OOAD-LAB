package com.sms.model;

import java.io.Serializable;

public abstract class User implements Serializable {
    protected String userID;
    protected String name;
    protected String email;
    protected String password;
    protected String role;

    public User(String userID, String name, String email, String password, String role) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean login(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    public void logout() {
        // Logout logic
    }

    // Getters and Setters
    public String getUserID() { return userID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
