package com.avito.merchshop.model;

import com.avito.merchshop.domain.Inventory;
import lombok.Data;

@Data
public class InventoryItem {
  private String type;
  private Integer quantity;

  public InventoryItem(Inventory inventory) {
    this.type = inventory.getItem();
    this.quantity = inventory.getAmount();
  }
}
