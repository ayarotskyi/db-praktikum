package com.db.praktikum;
import com.db.praktikum.entity.*;
import com.db.praktikum.utils.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.*;


public class App {

    static Session session;
    static String red = "\u001B[31m";
    static String green = "\u001B[32m";
    static String reset = "\u001B[0m";


    public static void main( String[] args ){


        init();
        System.out.println("Hello, welcome to Database Application.");
        boolean isRunning = true;
        Scanner mainScanner = new Scanner(System.in);

        while (isRunning){

            
            System.out.print("Enter 1 to get information about selected product.\n" +
                    "Enter 2 to find all products with given pattern.\n" +
                    "Enter 8 to find all Trolls.\n" +
                    "Enter 9 to get all offers for given Product ID.\n" +
                    "Enter \"exit\" to finish session.\n" +
                    "Enter your choice: ");
            String option = mainScanner.nextLine();
            System.out.println();


            switch (option){
                case "1":{
                    getProduct();
                    break;
                }
                case "2":{
                    getProducts();
                    break;
                }
                case "3":{
                    test();
                    break;
                }
                case "4":{}
                case "5":{}
                case "6":{}
                case "7":{}
                case "8":{
                    getTrolls();
                    break;
                }
                case "9":{
                    getOffers();
                    break;
                }

                case "exit":{
                 System.out.println("Thanks for using our application!");
                 isRunning = false;
                 break;
                }
                default:{
                    System.out.println("Invalid input, try again!\n");
                    break;
                }
            }
            
        }
        mainScanner.close();
        finish();
    }

    public static void test(){
        String hql = "FROM GuestFeedback ";
        Query<GuestFeedback> query = session.createQuery(hql, GuestFeedback.class);
        List<GuestFeedback> list = query.getResultList();

        for(GuestFeedback k : list)
            System.out.println(k.toString());

        System.out.println(list.size());
    }

    public static void init(){
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
    }

    public static void finish(){
        session.close();
    }

    public static void getProduct(){
        while(true){
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter ProductID to get information about selected product or type \"exit\" to return to main menu \n" + "Your input: ");
            String choice = scanner.nextLine();

            if(choice.equals("exit")){
                System.out.println();
                break;
            }


            String hql = "FROM Product WHERE ProductAsin = :userInput";
            try {
                Product product = session.createQuery(hql, Product.class).setParameter("userInput", choice).getSingleResult();
                System.out.println(product.toString() + "\n");
            }catch (Exception e){
                System.out.println("Product with this ID does not exist or invalid command, try again!\n");
            }
        }
    }

    public static void getProducts(){


        while(true){
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter pattern to find all matching products or type \"exit\" to return to main menu \n" + "Your input: ");
            String choice = scanner.nextLine();

            if(choice.equals("exit")){
                System.out.println();
                break;
            }


            String hql;
            Query<Product> query;

            if(choice.equals("")){
                hql = "FROM Product";
                query = session.createQuery(hql, Product.class);
            }else {
                hql = "FROM Product WHERE Title LIKE :userInput";
                query = session.createQuery(hql, Product.class).setParameter("userInput", choice);
            }



            List<Product> productList = query.getResultList();
            if(productList.isEmpty()) {
                System.out.println("Products with given pattern does not exist!\n");
            } else {
                for (Product product : productList)
                    System.out.println(product.toString());
                System.out.println();
            }
        }
    }

    public static void getTrolls(){

        while(true){
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the limit value. All users with a value below limit value will be considered trolls.\n"
                    + "Type \"exit\" to return to main menu\n"
                    + "Your input: ");
            String choice = scanner.nextLine();

            if(choice.equals("exit")){
                System.out.println();
                break;
            }

            try {
                Double.parseDouble(choice);
            }catch (Exception e){
                System.out.println("That was not a number, try again! \n");
                continue;
            }


            String hql =
                    "SELECT DISTINCT f.feedbackPK.username.Username " +
                    "FROM Feedback f GROUP BY f.feedbackPK.username HAVING AVG(f.helpful) < :userInput";
            Query<String> query = session.createQuery(hql, String.class).setParameter("userInput", Double.parseDouble(choice));
            List<String> result = query.getResultList();
            System.out.println(result.toString() + "\n");
        }
    }

    public static void getOffers(){
        while(true){

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter ProductID to get all available offers t or type \"exit\" to return to main menu \n" + "Your input: ");
            String choice = scanner.nextLine();

            if(choice.equals("exit")){
                System.out.println();
                break;
            }


            String hql = "FROM ProductInFilliale p WHERE p.productAsin.ProductAsin = :userInput and p.avail = TRUE";
            List<ProductInFilliale> productList = session.createQuery(hql, ProductInFilliale.class).setParameter("userInput", choice).getResultList();

            if(productList.isEmpty()){
                System.out.println("No offers for given product are available now!\n");
                continue;
            }


            for(ProductInFilliale product : productList)
                System.out.println(product.toString());
            System.out.println();
        }
    }
}
