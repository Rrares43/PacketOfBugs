package com.example.springreddit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// am facut aici asa de test clasa asta post o modifici tu cand e doar ca sa imi dispara erorile alea de import si ca clasa nu exista

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Post() {
    }

    public Long getId() {
        return id;
    }
}