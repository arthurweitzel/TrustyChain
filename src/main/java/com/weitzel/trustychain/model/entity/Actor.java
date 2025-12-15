package com.weitzel.trustychain.model.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "actors")
public class Actor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


}
