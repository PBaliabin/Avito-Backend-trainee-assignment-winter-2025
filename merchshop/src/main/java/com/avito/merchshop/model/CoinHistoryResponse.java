package com.avito.merchshop.model;

import java.util.List;
import lombok.Data;

@Data
public class CoinHistoryResponse {
  private List<CoinHistoryItemReceived> received;
  private List<CoinHistoryItemSent> sent;
}
