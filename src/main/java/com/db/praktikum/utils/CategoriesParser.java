package com.db.praktikum.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
            NodeList categoryNodes = root.getElementsByTagName("category");

            insertCategories(categoryNodes, null, connection);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Categories parsed.");

    }

    private static void insertCategories(NodeList nodes, Integer parentCategory, Connection connection) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                try {
                    PreparedStatement categoryStatement = connection.prepareStatement(
                            "INSERT INTO Category (categoryName, parentCategory) " +
                                    "VALUES (?, ?)",
                            Statement.RETURN_GENERATED_KEYS);

                    // Prepare statement for product-to-category insertion
                    PreparedStatement productToCategoryStatement = connection.prepareStatement(
                            "INSERT INTO ProductToCategory (ProductAsin, categoryId) " +
                                    "VALUES (?, ?)");

                    Element element = (Element) node;
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

                    NodeList childNodes = element.getElementsByTagName("category");
                    if (childNodes.getLength() > 0) {
                        // Recursive call to insert child categories
                        insertCategories(childNodes, categoryKey, connection);
                    } else {
                        NodeList itemNodes = element.getElementsByTagName("item");
                        for (int j = 0; j < itemNodes.getLength(); j++) {
                            Node itemNode = itemNodes.item(j);
                            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                                String productAsin = itemNode.getTextContent().trim();

                                // Insert product-to-category mapping into ProductToCategory table
                                productToCategoryStatement.setString(1, productAsin);
                                productToCategoryStatement.setInt(2, categoryKey);
                                productToCategoryStatement.executeUpdate();
                            }
                        }
                    }
                } catch (SQLException e) {
                    ErrorHandler.handleError(connection, "Category", e);
                }
            }
        }
    }
}
