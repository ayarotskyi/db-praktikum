package com.db.praktikum;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.db.praktikum.utils.CategoriesParser;
import com.db.praktikum.utils.ErrorHandler;
import com.db.praktikum.utils.FeedbackParser;

import java.sql.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class XMLParser {
    public static void main(String[] args) {
        try {
            // Database connection details
            String url = "jdbc:postgresql://localhost:5432/test25";
            String username = "postgres";
            String password = "dbpraktikum25";

            Connection connection = DriverManager.getConnection(url, username, password);

            executeStartupSql(connection);

            parseStore(connection, "src/leipzig.xml");
            parseStore(connection, "src/dresden.xml");

            // CategoriesParser.parseCategories(connection);

            FeedbackParser.parse(connection);

            connection.close();
            System.out.println("Data inserted successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseStore(Connection connection, String filePath) throws Exception {
        try {
            // Create DocumentBuilderFactory and DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(filePath));

            Element shopElement = document.getDocumentElement();
            NodeList itemNodeList = shopElement.getElementsByTagName("item");

            Element shopData = (Element) document.getElementsByTagName("shop").item(0);
            insertShop(connection, shopData);

            for (int i = 0; i < itemNodeList.getLength(); i++) {

                Element itemElement = (Element) itemNodeList.item(i);
                try {
                    insertProduct(connection, itemElement);
                } catch (Exception e) {
                    ErrorHandler.handleError(connection, "Product", e);
                }
                try {
                    insertProductInFilliale(connection, itemElement);
                } catch (Exception e) {
                    ErrorHandler.handleError(connection, "ProductInFilliale", e);
                }
                try {
                    insertBook(connection, itemElement);
                } catch (Exception e) {
                    ErrorHandler.handleError(connection, "Book", e);
                }
                try {
                    insertDVD(connection, itemElement);
                } catch (Exception e) {
                    ErrorHandler.handleError(connection, "DVD", e);
                }
                try {
                    insertMusic(connection, itemElement);
                } catch (Exception e) {
                    ErrorHandler.handleError(connection, "Music", e);
                }
            }

            System.out.println("Data from " + filePath + " inserted successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executeStartupSql(Connection connection) throws Exception {
        String sqlScriptFile = "initial.sql";
        Statement statement = connection.createStatement();
        BufferedReader reader = new BufferedReader(new FileReader(sqlScriptFile));

        StringBuilder scriptContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            scriptContent.append(line);
            scriptContent.append(System.lineSeparator());
        }
        reader.close();

        String sqlScript = scriptContent.toString();
        statement.execute(sqlScript);
        System.out.println("Database initialized successfully.");
    }

    private static void insertShop(Connection connection, Element shopElement) {

        String name = shopElement.getAttribute("name");
        String street = shopElement.getAttribute("street");
        String zip = shopElement.getAttribute("zip");

        String query = "INSERT INTO Filliale (FName, FStreet, FZip) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query);) {

            statement.setString(1, name);
            statement.setString(2, street);
            statement.setString(3, zip);
            statement.executeUpdate();

        } catch (SQLException e) {
            ErrorHandler.handleError(connection, "Filliale", e);
        }
    }

    private static void insertProduct(Connection connection, Element itemElement) throws SQLException {

        // For Dresden.xml check
        if (itemElement.getAttributes().getLength() < 3)
            return;

        String titel;
        Integer salesrank = null;
        String asin = itemElement.getAttribute("asin");
        String picture;

        // Difference between Picture in Leipzig and Dresden
        if (itemElement.getElementsByTagName("details").getLength() > 0) {
            picture = itemElement.getElementsByTagName("details").item(0).getAttributes().getNamedItem("img")
                    .getNodeValue();
        } else {
            picture = itemElement.getAttribute("picture");
        }

        // Title check
        Element titelElement = (Element) itemElement.getElementsByTagName("title").item(0);
        if (titelElement != null) {
            titel = titelElement.getTextContent();
        } else {
            // System.out.println("Missing Title");
            return;
        }

        // SalesRank check
        String salesrankAttr = itemElement.getAttribute("salesrank");
        if (!salesrankAttr.isEmpty()) {
            salesrank = Integer.parseInt(salesrankAttr);

        } else {
            // System.out.println("Missing Salesrank");
            return;
        }

        // Inserting Query
        String query = "INSERT INTO Product (ProductAsin, Title, Salesrank, Bild) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, asin);
            statement.setString(2, titel);
            statement.setInt(3, salesrank);
            statement.setString(4, picture);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw e;
            // System.out.println(e.getMessage());
        }
    }

    private static void insertProductInFilliale(Connection connection, Element itemElement) {

        // For Dresden.xml
        if (itemElement.getAttributes().getLength() < 3)
            return;

        Element shopElement = (Element) itemElement.getParentNode();
        String shopName = shopElement.getAttribute("name");
        String shopStreet = shopElement.getAttribute("street");
        String shopZip = shopElement.getAttribute("zip");
        String productAsin = itemElement.getAttribute("asin");

        // State check
        String state;
        Element priceElement = (Element) itemElement.getElementsByTagName("price").item(0);
        if (!priceElement.getAttribute("state").isEmpty()) {
            state = priceElement.getAttribute("state");
        } else {
            // System.out.println("Missing state");
            return;
        }

        // Calculating Price
        String price = null;
        String priceValue = priceElement.getTextContent();
        String mult = priceElement.getAttribute("mult");
        String currency = priceElement.getAttribute("currency");
        if (!priceValue.isEmpty() && Integer.parseInt(priceValue) > 0 && !mult.isEmpty() && !currency.isEmpty()) {
            double tmp = Double.parseDouble(mult) * Integer.parseInt(priceValue);
            String formattedPrice = String.format("%.2f", tmp);
            price = formattedPrice + " " + currency;
        }

        // Setting Avail
        boolean avail;
        if (price != null) {
            avail = true;
        } else {
            avail = false;
        }

        String query = "INSERT INTO ProductInFilliale (ProductAsin, Fname, Fstreet, Fzip, IState, Price, Avail) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, productAsin);
            statement.setString(2, shopName);
            statement.setString(3, shopStreet);
            statement.setString(4, shopZip);
            statement.setString(5, state);
            statement.setString(6, price);
            statement.setBoolean(7, avail);
            statement.executeUpdate();
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }
    }

    private static void insertBook(Connection connection, Element itemElement) {

        // For Dresden.xml
        if (itemElement.getAttributes().getLength() < 3)
            return;

        String asin = itemElement.getAttribute("asin");
        String pgroop = itemElement.getAttribute("pgroup");

        if (!pgroop.equals("Book")) {
            return; // Not a book, skip insertion
        }

        String isbn = null;
        String publicationDate = null;
        Integer pages = null;

        // Checking bookspec
        NodeList bookspecElements = itemElement.getElementsByTagName("bookspec");
        if (bookspecElements.getLength() > 0) {
            Element bookspecElement = (Element) bookspecElements.item(0);

            NodeList isbnElements = bookspecElement.getElementsByTagName("isbn");
            if (isbnElements.getLength() > 0) {
                Element isbnElement = (Element) isbnElements.item(0);
                isbn = isbnElement.getAttribute("val");
            }
            if (isbn.isEmpty()) {
                // System.out.println("Missing isbn");
                return;
            }

            // Checking publication
            NodeList publicationElements = bookspecElement.getElementsByTagName("publication");
            if (publicationElements.getLength() > 0) {
                Element publicationElement = (Element) publicationElements.item(0);
                publicationDate = publicationElement.getAttribute("date");
            }
            if (publicationDate.isEmpty()) {
                // System.out.println("Missing publicationDate");
                return;
            }

            // Checking pages
            NodeList pagesElements = bookspecElement.getElementsByTagName("pages");
            if (pagesElements.getLength() > 0) {
                Element pagesElement = (Element) pagesElements.item(0);
                String pagesValue = pagesElement.getTextContent();
                if (!pagesValue.isEmpty()) {
                    pages = Integer.parseInt(pagesValue);
                }
                if (pages == null) {
                    // System.out.println("Missing pages");
                    return;
                }
            }
        }

        // Checking publisher
        NodeList publisherList = itemElement.getElementsByTagName("publisher");
        if (publisherList.getLength() == 0) {
            // System.out.println("Missing Publisher");
            return;
        }

        // Checking author
        NodeList authorList = itemElement.getElementsByTagName("author");
        if (authorList.getLength() == 0) {
            // System.out.println("Missing author");
            return;
        }

        // Insert into Book Table
        String query = "INSERT INTO Book (BookAsin, Isbn, Releasedate, Pages) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, asin);
            statement.setString(2, isbn);
            statement.setDate(3, Date.valueOf(publicationDate));
            statement.setInt(4, pages);
            statement.executeUpdate();
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }

        // Insert into PublisherToBook Table
        String publisherQuery = "INSERT INTO PublisherToBook  (BookAsin , publisherName ) VALUES (?, ?)";
        try (PreparedStatement publisherStatement = connection.prepareStatement(publisherQuery)) {
            for (int i = 0; i < publisherList.getLength(); i++) {

                String publisherName;
                Element publisherElement = (Element) publisherList.item(i);

                // Difference between Dresden and Leipzig publisher data structure
                if (publisherElement.getAttributes().getLength() != 0)
                    publisherName = publisherElement.getAttribute("name");
                else
                    publisherName = publisherElement.getTextContent();

                if (!publisherName.isEmpty()) {
                    publisherStatement.setString(1, asin);
                    publisherStatement.setString(2, publisherName);
                    publisherStatement.executeUpdate();
                }

            }
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }

        // Insert into AuthorToBook Table
        String authorQuery = "INSERT INTO AuthorToBook  (BookAsin , authorName  ) VALUES (?, ?)";
        try (PreparedStatement authortatement = connection.prepareStatement(authorQuery)) {
            for (int i = 0; i < authorList.getLength(); i++) {

                String authorName;
                Element authorElement = (Element) authorList.item(i);

                // Difference between Dresden and Leipzig publisher data structure
                if (authorElement.getAttributes().getLength() != 0) {
                    authorName = authorElement.getAttribute("name");
                } else {
                    authorName = authorElement.getTextContent();
                }

                if (!authorName.isEmpty()) {
                    authortatement.setString(1, asin);
                    authortatement.setString(2, authorName);
                    authortatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }
    }

    private static void insertMusic(Connection connection, Element itemElement) {

        // For Dresden.xml
        if (itemElement.getAttributes().getLength() < 3)
            return;

        String asin = itemElement.getAttribute("asin");
        String pgroop = itemElement.getAttribute("pgroup");

        if (!pgroop.equals("Music")) {
            return; // Not a Music item, skip insertion
        }

        // Checking musicspec
        Element musicspecElement = null;
        NodeList musicspecList = itemElement.getElementsByTagName("musicspec");
        if (musicspecList.getLength() > 0) {
            musicspecElement = (Element) musicspecList.item(0);
        }

        // Checking releasedate
        String releaseDate = null;
        if (musicspecElement != null) {
            NodeList releaseDateList = musicspecElement.getElementsByTagName("releasedate");
            if (releaseDateList.getLength() > 0) {
                releaseDate = releaseDateList.item(0).getTextContent();
            }
        }
        if (releaseDate.isEmpty()) {
            // System.out.println("Missing releaseDate");
            return;
        }

        // Checking label
        NodeList labelList = itemElement.getElementsByTagName("label");
        if (labelList.getLength() == 0) {
            // System.out.println("Missing Label");
            return;
        }

        // Checking title
        NodeList tracksList = itemElement.getElementsByTagName("title");
        if (tracksList.getLength() == 0) {
            // System.out.println("Missing Tracks");
            return;
        }

        // Checking artist
        NodeList artistList = itemElement.getElementsByTagName("artist");
        if (artistList.getLength() == 0) {
            // System.out.println("Missing Artist");
            return;
        }

        // Insert into Music table
        String query = "INSERT INTO Music (MusicAsin, Releasedate) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, asin);
            statement.setDate(2, Date.valueOf(releaseDate));
            statement.executeUpdate();
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }

        // Insert into LabelToMusic table
        String labelQuery = "INSERT INTO LabelToMusic (MusicAsin, LabelName) VALUES (?, ?)";
        try (PreparedStatement labelStatement = connection.prepareStatement(labelQuery)) {
            for (int i = 0; i < labelList.getLength(); i++) {

                String labelName;
                Element labelElement = (Element) labelList.item(i);

                // Difference between Dresden and Leipzig label data structure
                if (labelElement.getAttributes().getLength() != 0) {
                    labelName = labelElement.getAttribute("name");
                } else {
                    labelName = labelElement.getTextContent();
                }

                if (!labelName.isEmpty()) {
                    labelStatement.setString(1, asin);
                    labelStatement.setString(2, labelName);
                    labelStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }

        // Insert into TracksToMusic table
        String tracksQuery = "INSERT INTO TrackToMusic  (MusicAsin, trackName) VALUES (?, ?)";
        try (PreparedStatement tracksStatement = connection.prepareStatement(tracksQuery)) {
            for (int i = 0; i < tracksList.getLength(); i++) {
                Element trackElement = (Element) tracksList.item(i);
                String trackTitle = trackElement.getTextContent();
                if (!trackTitle.isEmpty()) {
                    tracksStatement.setString(1, asin);
                    tracksStatement.setString(2, trackTitle);
                    tracksStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }

        // Insert into ArtistToMusic table
        String artistQuery = "INSERT INTO ArtistToMusic (MusicAsin, artistName) VALUES (?, ?)";
        try (PreparedStatement artistStatement = connection.prepareStatement(artistQuery);) {
            for (int i = 0; i < artistList.getLength(); i++) {

                String artistName;
                Element artistElement = (Element) artistList.item(i);

                // Difference between Dresden and Leipzig artist data structure
                if (artistElement.getAttributes().getLength() != 0) {
                    artistName = artistElement.getAttribute("name");
                } else {
                    artistName = artistElement.getTextContent();
                }

                if (!artistName.isEmpty()) {
                    artistStatement.setString(1, asin);
                    artistStatement.setString(2, artistName);
                    artistStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }
    }

    private static void insertDVD(Connection connection, Element itemElement) {

        // For Dresden.xml
        if (itemElement.getAttributes().getLength() < 3)
            return;

        String asin = itemElement.getAttribute("asin");
        String pgroop = itemElement.getAttribute("pgroup");

        if (!pgroop.equals("DVD")) {
            return; // Not a DVD, skip insertion
        }

        String format = null;
        String regionCode = null;
        Integer runningTime = null;
        Element dvdspecElement = null;

        // Checking dvdspec
        NodeList dvdspecList = itemElement.getElementsByTagName("dvdspec");
        if (dvdspecList.getLength() > 0) {
            dvdspecElement = (Element) dvdspecList.item(0);
        }

        if (dvdspecElement != null) {

            // Checking format
            NodeList formatList = dvdspecElement.getElementsByTagName("format");
            if (formatList.getLength() > 0) {
                format = formatList.item(0).getTextContent();
            }
            if (format.isEmpty()) {
                // System.out.println("Missing Format");
                return;
            }

            // Checking RegionCode
            NodeList regionCodeList = dvdspecElement.getElementsByTagName("regioncode");
            if (regionCodeList.getLength() > 0) {
                regionCode = regionCodeList.item(0).getTextContent();
            }
            if (regionCode.isEmpty()) {
                // System.out.println("Missing Regioncode");
                return;
            }

            // Checking Runtime
            NodeList runningTimeList = dvdspecElement.getElementsByTagName("runningtime");
            if (runningTimeList.getLength() > 0) {
                String runningTimeStr = runningTimeList.item(0).getTextContent();
                if (!runningTimeStr.isEmpty()) {
                    runningTime = Integer.parseInt(runningTimeStr);
                }
                if (runningTime == null) {
                    // System.out.println("Missing Runtime");
                    return;
                }
            }
        }

        String query = "INSERT INTO DVD (DVDAsin, Format, RegionCode, RunTime) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, asin);
            statement.setString(2, format);
            statement.setString(3, regionCode);
            statement.setInt(4, runningTime);
            statement.executeUpdate();
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }

        NodeList actorList = itemElement.getElementsByTagName("actor");
        String actorQuery = "INSERT INTO ActorToDVD (DVDAsin , actorName  ) VALUES (?, ?)";
        try (PreparedStatement actorstatement = connection.prepareStatement(actorQuery)) {
            for (int i = 0; i < actorList.getLength(); i++) {

                String actorName;
                Element actorElement = (Element) actorList.item(i);

                // Difference between Dresden and Leipzig actor data structure
                if (actorElement.getAttributes().getLength() != 0) {
                    actorName = actorElement.getAttribute("name");
                } else {
                    actorName = actorElement.getTextContent();
                }
                if (!actorName.isEmpty()) {
                    actorstatement.setString(1, asin);
                    actorstatement.setString(2, actorName);
                    actorstatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }

        NodeList directorList = itemElement.getElementsByTagName("director");
        String directorQuery = "INSERT INTO DirectorToDVD (DVDAsin , directorname) VALUES (?, ?)";
        try (PreparedStatement directorstatement = connection.prepareStatement(directorQuery)) {
            for (int i = 0; i < directorList.getLength(); i++) {

                String directorName;
                Element directorElement = (Element) directorList.item(i);

                // Difference between Dresden and Leipzig director data structure
                if (directorElement.getAttributes().getLength() != 0) {
                    directorName = directorElement.getAttribute("name");
                } else {
                    directorName = directorElement.getTextContent();
                }
                if (!directorName.isEmpty()) {
                    directorstatement.setString(1, asin);
                    directorstatement.setString(2, directorName);
                    directorstatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }

        NodeList creatorList = itemElement.getElementsByTagName("creator");
        String creatorQuery = "INSERT INTO CreatorToDVD (DVDAsin , creatorname) VALUES (?, ?)";
        try (PreparedStatement creatorstatement = connection.prepareStatement(creatorQuery)) {
            for (int i = 0; i < creatorList.getLength(); i++) {

                String creatorName;
                Element creatorElement = (Element) creatorList.item(i);

                // Difference between Dresden and Leipzig creator data structure
                if (creatorElement.getAttributes().getLength() != 0) {
                    creatorName = creatorElement.getAttribute("name");
                } else {
                    creatorName = creatorElement.getTextContent();
                }

                if (!creatorName.isEmpty()) {
                    creatorstatement.setString(1, asin);
                    creatorstatement.setString(2, creatorName);
                    creatorstatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            // System.out.println(e.getMessage());
        }
    }

}