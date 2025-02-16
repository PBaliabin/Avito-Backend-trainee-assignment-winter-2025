package com.avito.merchshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "transaction_merch_shop")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "sender", nullable = false)
  private String sender;

  @Column(name = "receiver", nullable = false)
  private String receiver;

  @Column(name = "amount", nullable = false)
  private int amount;
}
