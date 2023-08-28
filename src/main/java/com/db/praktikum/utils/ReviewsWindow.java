package com.db.praktikum.utils;

import com.db.praktikum.App;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

public class ReviewsWindow extends JFrame {
    private JTextArea reviewsTextArea;
    private JButton addReviewButton;
    private JButton refreshButton;

    public ReviewsWindow() {
        setTitle("Reviews");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        JPanel mainPanel = new JPanel(new BorderLayout());


        reviewsTextArea = new JTextArea();
        reviewsTextArea.setEditable(false);
        reviewsTextArea.setLineWrap(true);
        reviewsTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(reviewsTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel();
        addReviewButton = new JButton("Add New Review");
        buttonPanel.add(addReviewButton);
        refreshButton = new JButton("Refresh");
        buttonPanel.add(refreshButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        refreshReviews(App.readReviews());






        addReviewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReviewEntryDialog dialog = new ReviewEntryDialog();
                dialog.setVisible(true);
                if (dialog.isDataSaved()) {
                    String usernameOrGuest = dialog.getUsernameOrGuest();
                    String productID = dialog.getProductID();
                    String message = dialog.getMessage();
                    int rating = dialog.getRating();

                    App.writeReviews(rating,message,productID,usernameOrGuest);
                }

            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshReviews(App.readReviews());
            }
        });
    }

    private void refreshReviews(List<String> reviews) {
        reviewsTextArea.setText("");
        for (String review : reviews) {
            reviewsTextArea.append(review + "\n\n");
        }
        reviewsTextArea.append("Reviews successfully updated, total number of review is " + reviews.size());
    }
}

class ReviewEntryDialog extends JDialog {
    private JTextField usernameOrGuestField;
    private JTextField productIDField;
    private JTextArea messageTextArea;
    private JComboBox<Integer> ratingComboBox;
    private boolean dataSaved = false;

    public ReviewEntryDialog() {
        setTitle("Add New Review");
        setSize(400, 300);
        setModal(true);

        // Create and layout components
        JPanel mainPanel = new JPanel(new GridLayout(5, 2));
        usernameOrGuestField = new JTextField();
        productIDField = new JTextField();
        messageTextArea = new JTextArea();
        ratingComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JButton saveButton = new JButton("Save");

        mainPanel.add(new JLabel("Username/Guest:"));
        mainPanel.add(usernameOrGuestField);
        mainPanel.add(new JLabel("Product ID:"));
        mainPanel.add(productIDField);
        mainPanel.add(new JLabel("Message:"));
        mainPanel.add(new JScrollPane(messageTextArea));
        mainPanel.add(new JLabel("Rating:"));
        mainPanel.add(ratingComboBox);
        mainPanel.add(new JLabel());
        mainPanel.add(saveButton);

        add(mainPanel);

        // Save button action
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dataSaved = true;
                dispose();
            }
        });
    }

    public String getUsernameOrGuest() {
        return usernameOrGuestField.getText();
    }

    public String getProductID() {
        return productIDField.getText();
    }

    public String getMessage() {
        return messageTextArea.getText();
    }

    public int getRating() {
        return (int) ratingComboBox.getSelectedItem();
    }

    public boolean isDataSaved() {
        return dataSaved;
    }
}
