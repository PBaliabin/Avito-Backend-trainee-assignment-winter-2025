package com.avito.merchshop.model.request;

import lombok.Data;

@Data
public class SendCoinRequest {
  private String toUser;
  private Integer amount;
}
