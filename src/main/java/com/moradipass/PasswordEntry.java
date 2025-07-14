package com.moradipass;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PasswordEntry {
    private final StringProperty site;
    private final StringProperty username;
    private final StringProperty password;

    public PasswordEntry(String site, String username, String password) {
        this.site = new SimpleStringProperty(site);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
    }

    public String getSite() { return site.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }

    public void setSite(String site) { this.site.set(site); }
    public void setUsername(String username) { this.username.set(username); }
    public void setPassword(String password) { this.password.set(password); }

    public StringProperty siteProperty() { return site; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
}
