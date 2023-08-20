package com.db.praktikum.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.opencsv.CSVReader;

public class FeedbackParser {
    public static void parse(Connection connection) throws Exception {
        String csvFilePath = "src/reviews.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
                CSVReader csvReader = new CSVReader(reader)) {

            csvReader.readNext(); // Read the header line
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                String usernameValue = line[4]; // Get the "user" column value
                boolean isGuest = usernameValue.equals("guest"); // Determine if the user is a guest
                try {
                    if (!isGuest) {
                        PreparedStatement userStatement = connection.prepareStatement(
                                "INSERT INTO Kunde (Username) " +
                                        "VALUES (?) " +
                                        "ON CONFLICT (Username) DO NOTHING");
                        // Insert user information into Kunde table
                        userStatement.setString(1, usernameValue);
                        userStatement.executeUpdate();

                        PreparedStatement feedbackStatement = connection.prepareStatement(
                                "INSERT INTO Feedback (Username, ProductAsin, Rating, fMessage, Helpful) " +
                                        "VALUES (?, ?, ?, ?, ?)");

                        // Insert feedback information into the corresponding table
                        feedbackStatement.setString(1, usernameValue);
                        feedbackStatement.setString(2, line[0]); // "product" column
                        feedbackStatement.setInt(3, Integer.parseInt(line[1])); // "rating" column
                        feedbackStatement.setString(4, line[6]); // "content" column
                        feedbackStatement.setInt(5, Integer.parseInt(line[2]));
                        feedbackStatement.executeUpdate();
                        continue;
                    }

                    PreparedStatement guestFeedbackStatement = connection.prepareStatement(
                            "INSERT INTO GuestFeedback (ProductAsin, Rating, fMessage, Helpful) " +
                                    "VALUES (?, ?, ?, ?)");

                    guestFeedbackStatement.setString(1, line[0]); // "product" column
                    guestFeedbackStatement.setInt(2, Integer.parseInt(line[1])); // "rating" column
                    guestFeedbackStatement.setString(3, line[6]); // "content" column
                    guestFeedbackStatement.setInt(4, Integer.parseInt(line[2]));
                    guestFeedbackStatement.executeUpdate();
                } catch (Exception e) {
                    ErrorHandler.handleError(connection, isGuest ? "GuestFeedback" : "Feedback", e);
                }
            }

            System.out.println("Feedbacks parsed successfully.");

        } catch (Exception e) {
            throw e;
        }
    }
}
