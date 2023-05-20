package com.db.praktikum;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLParser {
    public static void main(String[] args) {
        // Database connection parameters
        String url = "jdbc:postgresql://localhost:5455/greensoft";
        String username = "root";
        String password = "pwd";

        try {
            Class.forName("org.postgresql.Driver");
            // Create a database connection
            Connection conn = DriverManager.getConnection(url, username, password);

            // Read the XML file
            File xmlFile = new File("src/products.xml");

            // Create a DOM parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            Document categoriesDocument = builder.parse("src/categories.xml");

            PreparedStatement categoriesStatement = conn
                    .prepareStatement("INSERT INTO \"Category\" (name, parent_name) VALUES (?, ?)");
            Element caetgoriesRoot = categoriesDocument.getDocumentElement();

            // Parse and insert data into the tables
            insertCategories(caetgoriesRoot, categoriesStatement, null);

            conn.close();
            System.out.println("Data insertion completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertCategories(Element element, PreparedStatement statement, String parentName)
            throws SQLException {
        NodeList categoryList = element.getElementsByTagName("category");

        for (int i = 0; i < categoryList.getLength(); i++) {
            Node categoryNode = categoryList.item(i);

            if (categoryNode.getNodeType() == Node.ELEMENT_NODE) {
                Element categoryElement = (Element) categoryNode;

                String categoryText = categoryElement.getTextContent().trim();

                // Extract the substring before the new line character
                int newlineIndex = categoryText.indexOf("\n");
                String categoryName = newlineIndex == -1 ? categoryText : categoryText.substring(0, newlineIndex);

                // Set the values in the prepared statement
                statement.setString(1, categoryName);
                if (parentName == null) {
                    statement.setNull(2, Types.NULL);
                } else {
                    statement.setString(2, parentName);
                }

                try {
                    // Execute the statement to insert the data
                    statement.executeUpdate();
                } catch (Exception e) {
                    System.out.println("Category " + categoryName + ". Error: " + e.getMessage());
                }

                // Recursively parse nested categories
                insertCategories(categoryElement, statement, categoryName);
            }
        }
    }
}