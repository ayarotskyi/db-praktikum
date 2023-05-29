package com.db.praktikum.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CategoriesParser {
    public static void parseCategories(Connection connection) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File("src/categories.xml"));

            // Get the root element
            Element root = document.getDocumentElement();
            NodeList immediateChildren = root.getChildNodes();

            List<Element> categories = new ArrayList<>();

            for (int i = 0; i < immediateChildren.getLength(); i++) {
                Node childNode = immediateChildren.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNode;
                    categories.add(childElement);
                    if (childElement.getTagName().equals("category")) {
                        categories.add(childElement);
                    }
                }
            }
            System.out.println("Parsing categories:");
            insertCategories(categories,
                    null, connection, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("Categories parsed.");
    }

    private static void insertCategories(List<Element> elements, Integer parentCategory, Connection connection,
            int recursion) {
        for (int i = 0; i < elements.size(); i++) {
            if (recursion == 0) {
                System.out.print("\r" + "Root categories: " + i + " / " + elements.size());
            }

            Element element = elements.get(i);

            try {
                PreparedStatement categoryStatement = connection.prepareStatement(
                        "INSERT INTO Category (categoryName, parentCategory) " +
                                "VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS);

                // Prepare statement for product-to-category insertion
                PreparedStatement productToCategoryStatement = connection.prepareStatement(
                        "INSERT INTO ProductToCategory (ProductAsin, categoryId) " +
                                "VALUES (?, ?)");

                String categoryName = element.getTextContent().trim();
                int lineEndIndex = categoryName.indexOf('\n');
                if (lineEndIndex != -1) {
                    categoryName = categoryName.substring(0, lineEndIndex);
                }

                // Insert category into Category table
                categoryStatement.setString(1, categoryName);
                categoryStatement.setObject(2, parentCategory);

                categoryStatement.executeUpdate();

                ResultSet generatedKeys = categoryStatement.getGeneratedKeys();

                if (!generatedKeys.next()) {
                    continue;
                }

                int categoryKey = generatedKeys.getInt(1);

                NodeList immediateChildren = element.getChildNodes();
                List<Element> categories = new ArrayList<>();
                List<Element> items = new ArrayList<>();

                for (int j = 0; j < immediateChildren.getLength(); j++) {
                    Node childNode = immediateChildren.item(j);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element childElement = (Element) childNode;
                        if (childElement.getTagName().equals("category")) {
                            categories.add(childElement);
                        } else if (childElement.getTagName().equals("item")) {
                            items.add(childElement);
                        }
                    }
                }

                for (int j = 0; j < items.size(); j++) {
                    Element itemElement = items.get(j);
                    String productAsin = itemElement.getTextContent().trim();

                    // Insert product-to-category mapping into ProductToCategory table
                    productToCategoryStatement.setString(1, productAsin);
                    productToCategoryStatement.setInt(2, categoryKey);
                    productToCategoryStatement.executeUpdate();
                }

                insertCategories(categories, categoryKey, connection, recursion + 1);

            } catch (SQLException e) {
                ErrorHandler.handleError(connection, "Category", e);
            }

        }
    }
}
