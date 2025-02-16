package com.avito.merchshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Entity
@Table(name = "item_merch_shop")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "item", nullable = false)
    private String item;

    @Column(name = "price", nullable = false)
    private Integer price;
}
