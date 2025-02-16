package com.avito.merchshop.model;

import com.avito.merchshop.domain.Transaction;
import lombok.Data;

@Data
public class CoinHistoryItemReceived {
  private String fromUser;
  private Integer amount;

  public CoinHistoryItemReceived(Transaction transaction) {
    this.fromUser = transaction.getSender();
    this.amount = transaction.getAmount();
  }
}
