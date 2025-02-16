package com.avito.merchshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventory_item_merch_shop")
public class Inventory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "owner", nullable = false)
  private String owner;

  @Column(name = "item", nullable = false)
  private String item;

  @Column(name = "amount", nullable = false)
  private Integer amount;
}
