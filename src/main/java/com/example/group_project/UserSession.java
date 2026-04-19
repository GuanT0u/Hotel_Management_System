package com.example.group_project;

// its a global static class using for store the info of action by the currently logged user
public class UserSession {
    private static int currentUserId = -1;
    private static String currentUserRole = "";

    public static void login(int userId, String role) {
        currentUserId = userId;
        currentUserRole = role;
    }

    public static void logout() {
        currentUserId = -1;
        currentUserRole = "";
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }
}