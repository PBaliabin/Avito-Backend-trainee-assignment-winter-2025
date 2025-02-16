package com.avito.merchshop.model.response;

import com.avito.merchshop.model.CoinHistoryResponse;
import com.avito.merchshop.model.InventoryItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
public class GetInfoResponse {
    private Integer coins;
    private List<InventoryItem> inventory;
    private CoinHistoryResponse coinHistory;
}
