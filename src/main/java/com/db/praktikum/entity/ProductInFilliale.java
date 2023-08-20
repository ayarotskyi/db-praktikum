package com.db.praktikum.entity;
import javax.persistence.*;



@Entity
@Table(name = "ProductInFilliale")
public class ProductInFilliale {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ProductInFillialeId")
    private Long id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "FName", referencedColumnName = "FName"),
            @JoinColumn(name = "FStreet", referencedColumnName = "FStreet"),
            @JoinColumn(name = "FZip", referencedColumnName = "FZip")})
    private Filliale filliale;

    @ManyToOne
    @JoinColumn(name = "ProductAsin",referencedColumnName = "ProductAsin")
    private Product productAsin;

    @Column(name = "IState")
    private String iState;

    @Column(name = "Price")
    private Double price;

    @Column(name = "Cur")
    private String currency;

    @Column(name = "Avail")
    private Boolean avail;


    public Long getId() {
        return id;
    }

    public String getFName() {
        return filliale.getFName();
    }

    public String getFStreet() {
        return filliale.getFStreet();
    }

    public String getFZip() {
        return filliale.getFZip();
    }

    public String getProductAsin() {
        return productAsin.getProductAsin();
    }

    public String getIState() {
        return iState;
    }

    public Double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public Boolean getAvail() {
        return avail;
    }

    @Override
    public String toString() {
        return "Filial: " + getFName() + " " + getFStreet() + " " + getFZip() +
                "; State: '" + getIState() + '\'' +
                "; price= " + price + currency + '\'' + ".";
    }
}
