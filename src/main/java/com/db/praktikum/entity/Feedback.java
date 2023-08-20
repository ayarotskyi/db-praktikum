package com.db.praktikum.entity;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "Feedback")
public class Feedback {
    @EmbeddedId
    private FeedbackPK feedbackPK;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "Helpful")
    private Integer helpful;

    @Column(name = "fMessage")
    private String fMessage;


    public String getUsername() {
        return feedbackPK.getUsername();
    }

    public String getProductAsin() {
        return feedbackPK.getProductAsin();
    }

    public FeedbackPK getFeedbackPK() {
        return feedbackPK;
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
        return "Feedback{" +
                "username=" + feedbackPK.getUsername() +
                ", productAsin=" + feedbackPK.getProductAsin() +
                ", rating=" + rating +
                ", helpful=" + helpful +
                ", fMessage='" + fMessage + '\'' +
                '}';
    }
}


@Embeddable
class FeedbackPK implements Serializable {

    @ManyToOne
    @JoinColumn(name = "Username",referencedColumnName = "Username")
    private Kunde username;

    @ManyToOne
    @JoinColumn(name = "ProductAsin",referencedColumnName = "ProductAsin")
    private Product productAsin;

    public String getUsername() {
        return username.getUsername();
    }


    public String getProductAsin() {
        return productAsin.getProductAsin();
    }
}
