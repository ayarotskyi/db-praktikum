package com.db.praktikum;

import com.db.praktikum.entity.*;
import com.db.praktikum.utils.CategoriesWindow;
import com.db.praktikum.utils.HibernateUtil;
import com.db.praktikum.utils.ReviewsWindow;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.*;

public class App {

    static Session session;
    static String red = "\u001B[31m";
    static String green = "\u001B[32m";
    static String reset = "\u001B[0m";

    public static void main(String[] args) {

        init();
        System.out.println("Hello, welcome to Database Application.");
        boolean isRunning = true;
        Scanner mainScanner = new Scanner(System.in);

        while (isRunning) {

            System.out.print("Enter 1 to get information about selected product.\n" +
                    "Enter 2 to find all products with given pattern.\n" +
                    "Enter 3 to show the tree of all categories.\n" +
                    "Enter 5 to find all top 'k' products.\n" +
                    "Enter 6 to find all similar cheaper products.\n" +
                    "Enter 7 to access reviews UI.\n" +
                    "Enter 8 to find all Trolls.\n" +
                    "Enter 9 to get all offers for given Product ID.\n" +
                    "Enter \"exit\" to finish session.\n" +
                    "Enter your choice: ");
            String option = mainScanner.nextLine();
            System.out.println();

            switch (option) {
                case "1": {
                    getProduct();
                    break;
                }
                case "2": {
                    getProducts();
                    break;
                }
                case "3": {
                    CategoriesWindow categoriesWindow = new CategoriesWindow(session);
                    categoriesWindow.setVisible(true);
                    break;
                }
                case "4": {
                }
                case "5": {
                    getTopProducts();
                    break;
                }
                case "6": {
                    getSimilarCheaperProduct();
                    break;
                }
                case "7": {
                    ReviewsWindow reviewsWindow = new ReviewsWindow();
                    reviewsWindow.setVisible(true);
                    break;
                }
                case "8": {
                    getTrolls();
                    break;
                }
                case "9": {
                    getOffers();
                    break;
                }

                case "exit": {
                    System.out.println("Thanks for using our application!");
                    isRunning = false;
                    break;
                }
                default: {
                    System.out.println("Invalid command, try again!\n");
                    break;
                }
            }

        }
        mainScanner.close();
        finish();

    }

    public static void init() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
    }

    public static void finish() {
        session.close();
    }

    public static void getProduct() {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print(
                    "Enter ProductID to get information about selected product or type \"exit\" to return to main menu \n"
                            + "Your input: ");
            String choice = scanner.nextLine();

            if (choice.equals("exit")) {
                System.out.println();
                break;
            }

            String hql = "FROM Product WHERE ProductAsin = :userInput";
            try {
                Product product = session.createQuery(hql, Product.class).setParameter("userInput", choice)
                        .getSingleResult();
                System.out.println(product.toString() + "\n");
            } catch (Exception e) {
                System.out.println("Product with this ID does not exist or invalid command, try again!\n");
            }
            scanner.close();
        }
    }

    public static void getProducts() {

        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter pattern to find all matching products or type \"exit\" to return to main menu \n"
                    + "Your input: ");
            String choice = scanner.nextLine();

            if (choice.equals("exit")) {
                System.out.println();
                break;
            }

            String hql;
            Query<Product> query;

            if (choice.equals("")) {
                hql = "FROM Product";
                query = session.createQuery(hql, Product.class);
            } else {
                hql = "FROM Product WHERE Title LIKE :userInput";
                query = session.createQuery(hql, Product.class).setParameter("userInput", choice);
            }

            List<Product> productList = query.getResultList();
            if (productList.isEmpty()) {
                System.out.println("Products with given pattern does not exist!\n");
            } else {
                for (Product product : productList)
                    System.out.println(product.toString());
                System.out.println();
            }
            scanner.close();
        }
    }

    public static void getCategoryTree() {

    }

    public static void getTopProducts() {

        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out
                    .print("Enter the number 'k'. After that will be shown top k products depending on their rating.\n"
                            + "Type \"exit\" to return to main menu\n"
                            + "Your input: ");
            String choice = scanner.nextLine();

            if (choice.equals("exit")) {
                System.out.println();
                break;
            }

            try {
                int tmp = Integer.parseInt(choice);
                if (tmp < 0) {
                    System.out.println("Number can not be negative, try again!\n");
                    continue;
                }
            } catch (Exception e) {
                System.out.println("That was not a number, try again! \n");
                continue;
            }

            String hql = "select p.ProductAsin FROM Product p WHERE Rating <> null order by Rating desc, ProductAsin desc";
            Query<String> query = session.createQuery(hql, String.class).setMaxResults(Integer.parseInt(choice));
            List<String> result = query.getResultList();
            System.out.println(result.toString() + "\n");
            scanner.close();
        }

    }

    public static void getSimilarCheaperProduct() {

        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print(
                    "Enter ProductID to get all similar cheaper products or type \"exit\" to return to main menu \n"
                            + "Your input: ");
            String choice = scanner.nextLine();

            if (choice.equals("exit")) {
                System.out.println();
                break;
            }

            String hql = "SELECT pif.productAsin.ProductAsin " +
                    "FROM  SimilarProduct sp JOIN ProductInFilliale pif on sp.similarProduct.pnummer2.ProductAsin = pif.productAsin.ProductAsin "
                    +
                    "WHERE sp.similarProduct.pnummer1.ProductAsin = :userInput " +
                    "AND pif.price < (select pif2.price from ProductInFilliale pif2 where pif2.productAsin.ProductAsin = :userInput )";
            Query<String> query = session.createQuery(hql, String.class).setParameter("userInput", choice);
            List<String> result = query.getResultList();
            System.out.println(result);
            scanner.close();
        }
    }

    public static List<String> readReviews() {
        String hql = "FROM Feedback";
        Query<Feedback> query = session.createQuery(hql, Feedback.class);
        List<Feedback> result = query.getResultList();

        String hqlGuest = "FROM GuestFeedback";
        Query<GuestFeedback> queryGuest = session.createQuery(hqlGuest, GuestFeedback.class);
        List<GuestFeedback> resultGuest = queryGuest.getResultList();

        List<String> reviews = new ArrayList<>();
        for (Feedback tmp : result) {
            reviews.add(tmp.toString());
        }
        for (GuestFeedback tmp : resultGuest) {
            reviews.add(tmp.toString());
        }

        return reviews;
    } // USED IN ReviewsWindow

    public static void writeReviews(int rating, String message, String productAsin, String user) {

        Transaction transaction;
        Product product = session.get(Product.class, productAsin);
        if (product == null)
            return;

        if (user.equals("")) {
            transaction = session.beginTransaction();
            try {
                GuestFeedback guestFeedback = new GuestFeedback();
                guestFeedback.setfMessage(message);
                guestFeedback.setProductAsin(product);
                guestFeedback.setRating(rating);
                guestFeedback.setHelpful(null);
                session.save(guestFeedback);
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null)
                    transaction.rollback();
                System.out.println(e.getMessage());
            }
        } else {
            transaction = session.beginTransaction();
            Kunde kunde = session.get(Kunde.class, user);
            if (kunde != null) {
                try {
                    Feedback feedback = new Feedback();
                    feedback.setfMessage(message);
                    feedback.setFeedbackPK(kunde, product);
                    feedback.setRating(rating);
                    feedback.setHelpful(null);
                    session.save(feedback);
                    transaction.commit();
                } catch (Exception e) {
                    if (transaction != null)
                        transaction.rollback();
                    System.out.println(e.getMessage());
                }
            }
        }
    } // USED IN ReviewsWindow

    public static void getTrolls() {

        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print(
                    "Enter the limit value. All users with a value below limit value will be considered trolls.\n"
                            + "Type \"exit\" to return to main menu\n"
                            + "Your input: ");
            String choice = scanner.nextLine();

            if (choice.equals("exit")) {
                System.out.println();
                break;
            }

            try {
                Double.parseDouble(choice);
            } catch (Exception e) {
                System.out.println("That was not a number, try again! \n");
                continue;
            }

            String hql = "SELECT DISTINCT f.feedbackPK.username.Username " +
                    "FROM Feedback f GROUP BY f.feedbackPK.username HAVING AVG(f.helpful) < :userInput";
            Query<String> query = session.createQuery(hql, String.class).setParameter("userInput",
                    Double.parseDouble(choice));
            List<String> result = query.getResultList();
            System.out.println(result.toString() + "\n");
            scanner.close();
        }
    }

    public static void getOffers() {
        while (true) {

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter ProductID to get all available offers or type \"exit\" to return to main menu \n"
                    + "Your input: ");
            String choice = scanner.nextLine();

            if (choice.equals("exit")) {
                System.out.println();
                break;
            }

            String hql = "FROM ProductInFilliale p WHERE p.productAsin.ProductAsin = :userInput and p.avail = TRUE";
            List<ProductInFilliale> productList = session.createQuery(hql, ProductInFilliale.class)
                    .setParameter("userInput", choice).getResultList();

            if (productList.isEmpty()) {
                System.out.println("No offers for given product are available now!\n");
                continue;
            }

            for (ProductInFilliale product : productList)
                System.out.println(product.toString());
            System.out.println();
            scanner.close();
        }
    }
}
