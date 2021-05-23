package com.aphex.mytourassistent.repository.network.models;


/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
class LoggedInUser {

    private final String userId;
    private final String displayName;

    public LoggedInUser(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}