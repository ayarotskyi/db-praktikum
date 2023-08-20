package com.db.praktikum.entity;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "filliale")
public class Filliale {
    @EmbeddedId
    private FillialePK pk;

    public String getFName() {
        return pk.getFName();
    }

    public String getFStreet() {
        return pk.getFStreet();
    }

    public String getFZip() {
        return pk.getFZip();
    }


}

@Embeddable
class FillialePK implements  Serializable {

    @Column(name = "FName")
    private String FName;

    @Column(name = "FStreet")
    private String FStreet;

    @Column(name = "FZip")
    private String FZip;

    public String getFName() {
        return FName;
    }

    public String getFStreet() {
        return FStreet;
    }

    public String getFZip() {
        return FZip;
    }
}



