package com.hiru.expense.tracker.smartexpensetracker;

import java.sql.*;

public class MySQLFinanceRequest {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/finance_database";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "2003";

    public static String generateFinanceQuote() {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT quote FROM finance_quotes ORDER BY RAND() LIMIT 1";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getString("quote");
                } else {
                    return "No finance quotes available.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error getting finance quote from the database.";
        }
    }
}