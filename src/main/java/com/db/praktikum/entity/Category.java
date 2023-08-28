package com.db.praktikum.entity;


import javax.persistence.*;

@Entity
@Table(name = "Category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;


    @Column(name = "categoryName")
    private String categoryName;

    @ManyToOne
    @JoinColumn(name = "parentCategory",referencedColumnName = "Id")
    private Category parentCategory;

    public Long getId() {
        return id;
    }
    public String getCategoryName() {
        return categoryName;
    }
    public Long getParentCategory() {
        if(parentCategory != null)
            return parentCategory.getId();
        else
            return null;
    }
}
