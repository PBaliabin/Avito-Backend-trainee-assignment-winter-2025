package com.avito.merchshop.model;

import com.avito.merchshop.domain.Transaction;
import lombok.Data;

@Data
public class CoinHistoryItemSent {
  private String toUser;
  private Integer amount;

  public CoinHistoryItemSent(Transaction transaction) {
    this.toUser = transaction.getReceiver();
    this.amount = transaction.getAmount();
  }
}
