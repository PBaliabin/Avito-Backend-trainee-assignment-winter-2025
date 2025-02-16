package com.avito.merchshop.service;

import com.avito.merchshop.model.request.AuthRequest;
import com.avito.merchshop.model.request.SendCoinRequest;
import com.avito.merchshop.model.response.AuthResponse;
import com.avito.merchshop.model.response.BuyItemResponse;
import com.avito.merchshop.model.response.GetInfoResponse;
import com.avito.merchshop.model.response.SendCoinResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
  @NotNull
  ResponseEntity<GetInfoResponse> getInfo(@NotNull String token);

  @NotNull
  ResponseEntity<SendCoinResponse> sendCoin(
      @NotNull SendCoinRequest sendCoinRequest, @NotNull String token);

  @NotNull
  ResponseEntity<BuyItemResponse> buyItem(@NotNull String itemName, @NotNull String token);

  @NotNull
  ResponseEntity<AuthResponse> authUser(@NotNull AuthRequest authRequest);
}
