package com.avito.merchshop.controller;

import com.avito.merchshop.model.request.AuthRequest;
import com.avito.merchshop.model.request.SendCoinRequest;
import com.avito.merchshop.model.response.AuthResponse;
import com.avito.merchshop.model.response.BuyItemResponse;
import com.avito.merchshop.model.response.GetInfoResponse;
import com.avito.merchshop.model.response.SendCoinResponse;
import com.avito.merchshop.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiController {
  private UserService userService;

  @GetMapping("/info")
  public ResponseEntity<GetInfoResponse> getInfo(@RequestHeader("Authorization") String token) {
    return userService.getInfo(token);
  }

  @PostMapping("/sendCoin")
  public ResponseEntity<SendCoinResponse> sendCoin(
      @RequestBody SendCoinRequest sendCoinRequest, @RequestHeader("Authorization") String token) {
    return userService.sendCoin(sendCoinRequest, token);
  }

  @GetMapping("/buy/{item}")
  public ResponseEntity<BuyItemResponse> buyItem(
      @PathVariable String item, @RequestHeader("Authorization") String token) {
    return userService.buyItem(item, token);
  }

  @PostMapping("/auth")
  public ResponseEntity<AuthResponse> authUser(@RequestBody AuthRequest authRequest) {
    return userService.authUser(authRequest);
  }
}
