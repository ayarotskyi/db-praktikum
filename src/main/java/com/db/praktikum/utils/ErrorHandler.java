package com.db.praktikum.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ErrorHandler {
    public static void handleError(Connection connection, String entityName, Exception exception) {
        try {
            PreparedStatement guestFeedbackStatement = connection.prepareStatement(
                    "INSERT INTO Error (EntityName, ErrorMessage) " +
                            "VALUES (?, ?)");

            guestFeedbackStatement.setString(1, entityName);
            guestFeedbackStatement.setString(2, exception.getMessage());
            guestFeedbackStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
