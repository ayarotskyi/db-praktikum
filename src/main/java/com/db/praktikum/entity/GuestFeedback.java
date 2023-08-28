package com.db.praktikum.entity;


import javax.persistence.*;

@Entity
@Table(name = "GuestFeedback")
public class GuestFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "ProductAsin",referencedColumnName = "ProductAsin")
    private Product productAsin;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "Helpful")
    private Integer helpful;

    @Column(name = "fMessage")
    private String fMessage;


    public Long getId() {
        return id;
    }

    public String getProductAsin() {
        return productAsin.getProductAsin();
    }

    public Integer getRating() {
        return rating;
    }


    public String getfMessage() {
        return fMessage;
    }

    public void setProductAsin(Product productAsin) {
        this.productAsin = productAsin;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setfMessage(String fMessage) {
        this.fMessage = fMessage;
    }

    public void setHelpful(Integer helpful) {
        this.helpful = helpful;
    }

    @Override
    public String toString() {
        return "guest" + " about Product:" + getProductAsin() + " = [" + getfMessage() + "]";
    }
}
