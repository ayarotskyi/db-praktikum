package com.db.praktikum.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Product")
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductAsin")
    private String ProductAsin;

    @Column(name = "Title")
    private String Title;

    @Column(name = "Salesrank")
    private int Salesrank;

    @Column(name = "Bild")
    private String Bild;

    @Column(name = "Rating")
    private Double Rating;

    public String getProductAsin() {
        return ProductAsin;
    }

    public String getTitle() {
        return Title;
    }

    public int getSalesrank() {
        return Salesrank;
    }

    public String getBild() {
        return Bild;
    }

    public Double getRating() {
        return Rating;
    }

    public void setProductAsin(String productAsin) {
        ProductAsin = productAsin;
    }

    public void setBild(String bild) {
        Bild = bild;
    }

    public void setRating(Double rating) {
        Rating = rating;
    }

    public void setSalesrank(int salesrank) {
        Salesrank = salesrank;
    }

    public void setTitle(String title) {
        Title = title;
    }

    @Override
    public String toString() {
        return "Product{" +
                "ProductAsin='" + ProductAsin + '\'' +
                ", Title='" + Title + '\'' +
                ", Salesrank=" + Salesrank +
                ", Bild='" + Bild + '\'' +
                ", rating=" + Rating +
                '}';
    }
}
