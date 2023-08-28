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


    public String getfMessage() {
        return fMessage;
    }

    public void setFeedbackPK(Kunde username,Product productAsin) {
        FeedbackPK feedbackPK = new FeedbackPK();
        feedbackPK.setUsername(username);
        feedbackPK.setProductAsin(productAsin);
        this.feedbackPK = feedbackPK;
    }

    public void setHelpful(Integer helpful) {
        this.helpful = helpful;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setfMessage(String fMessage) {
        this.fMessage = fMessage;
    }

    @Override
    public String toString() {
        return "User: " + feedbackPK.getUsername() + " about Product: " + feedbackPK.getProductAsin() + " => [" + getfMessage() + "]";
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

    public void setUsername(Kunde username) {
        this.username = username;
    }

    public void setProductAsin(Product productAsin) {
        this.productAsin = productAsin;
    }
}
