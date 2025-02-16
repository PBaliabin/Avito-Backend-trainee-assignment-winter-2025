package com.avito.merchshop.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
public class CoinHistoryResponse {
    private List<CoinHistoryItemReceived> received;
    private List<CoinHistoryItemSent> sent;
}
