package com.db.praktikum.entity;
import javax.persistence.*;


@Entity
@Table(name = "Kunde")
public class Kunde {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "Username")
    private String Username;

    public String getUsername() {
        return Username;
    }
}
