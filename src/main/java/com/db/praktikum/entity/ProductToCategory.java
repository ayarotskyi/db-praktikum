package com.db.praktikum.entity;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "ProductToCategory")
public class ProductToCategory {
    @EmbeddedId
    private ProductToCategoryPK productToCategory;

    public Long getId() {
        return productToCategory.getCategoryId();
    }

    public String getProductAsin() {
        return productToCategory.getProductAsin();
    }
}

@Embeddable
class ProductToCategoryPK implements Serializable {

    @ManyToOne
    @JoinColumn(name = "categoryId",referencedColumnName = "Id")
    private Category categoryId;


    @ManyToOne
    @JoinColumn(name = "ProductAsin",referencedColumnName = "ProductAsin")
    private Product productAsin;

    public Long getCategoryId() {
        return categoryId.getId();
    }

    public String getProductAsin() {
        return productAsin.getProductAsin();
    }
}
