package com.db.praktikum.entity;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "SimilarProduct")
public class SimilarProduct {
    @EmbeddedId
    private SimilarProductPK similarProduct;

    public String getPnummer1() {
        return similarProduct.getPnummer1();
    }

    public String getPnummer2() {
        return similarProduct.getPnummer2();
    }

}


@Embeddable
class SimilarProductPK implements Serializable {

    @ManyToOne
    @JoinColumn(name = "Pnummer1",referencedColumnName = "ProductAsin")
    private Product pnummer1;


    @ManyToOne
    @JoinColumn(name = "Pnummer2",referencedColumnName = "ProductAsin")
    private Product pnummer2;

    String getPnummer1() {
        return pnummer1.getProductAsin();
    }

    String getPnummer2() {
        return pnummer2.getProductAsin();
    }
}
