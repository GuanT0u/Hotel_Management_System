package com.example.group_project;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;

    // Database credentials
    private DBConnection() {
        try {
            // reading properties
            Properties props = new Properties();
            // if properties in resources
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("database.properties")) {
                if (in == null) {
                    throw new RuntimeException("Sorry, unable to find database.properties");
                }
                props.load(in);
            }

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.username");
            String pass = props.getProperty("db.password");

            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, pass);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        } else {
            try {
                if (instance.getConnection().isClosed()) {
                    instance = new DBConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}