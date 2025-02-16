package com.avito.merchshop.model.response;

import com.avito.merchshop.model.CoinHistoryResponse;
import com.avito.merchshop.model.InventoryItem;
import java.util.List;
import lombok.Data;

@Data
public class GetInfoResponse {
  private Integer coins;
  private List<InventoryItem> inventory;
  private CoinHistoryResponse coinHistory;
}
