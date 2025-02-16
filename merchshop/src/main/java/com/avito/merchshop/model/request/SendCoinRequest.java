package com.avito.merchshop.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class SendCoinRequest {
    private String toUser;
    private Integer amount;
}
