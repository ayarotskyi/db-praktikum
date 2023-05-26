package com.db.praktikum.utils;

public class AttributeNotFoundException extends Exception {

    private String attributeName;

    public AttributeNotFoundException(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String getMessage() {
        return attributeName + " is Empty";
    }
}
