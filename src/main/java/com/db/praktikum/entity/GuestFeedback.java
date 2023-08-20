package com.db.praktikum.entity;


import javax.persistence.*;

@Entity
@Table(name = "GuestFeedback")
public class GuestFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    public Integer getHelpful() {
        return helpful;
    }

    public String getfMessage() {
        return fMessage;
    }

    @Override
    public String toString() {
        return "GuestFeedback{" +
                "id=" + id +
                ", productAsin=" + productAsin.getProductAsin() +
                ", rating=" + rating +
                ", helpful=" + helpful +
                ", fMessage='" + fMessage + '\'' +
                '}';
    }
}
