package com.avito.merchshop.service;

import com.avito.merchshop.domain.Inventory;
import com.avito.merchshop.domain.Item;
import com.avito.merchshop.domain.Transaction;
import com.avito.merchshop.domain.User;
import com.avito.merchshop.exception.BadRequestException;
import com.avito.merchshop.exception.InternalServerErrorException;
import com.avito.merchshop.exception.UnauthorizedException;
import com.avito.merchshop.jwt.JwtTokenService;
import com.avito.merchshop.model.CoinHistoryItemReceived;
import com.avito.merchshop.model.CoinHistoryItemSent;
import com.avito.merchshop.model.CoinHistoryResponse;
import com.avito.merchshop.model.InventoryItem;
import com.avito.merchshop.model.request.AuthRequest;
import com.avito.merchshop.model.request.SendCoinRequest;
import com.avito.merchshop.model.response.AuthResponse;
import com.avito.merchshop.model.response.BuyItemResponse;
import com.avito.merchshop.model.response.GetInfoResponse;
import com.avito.merchshop.model.response.SendCoinResponse;
import com.avito.merchshop.repository.InventoryRepository;
import com.avito.merchshop.repository.ItemRepository;
import com.avito.merchshop.repository.TransactionRepository;
import com.avito.merchshop.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor()
public class UserServiceImpl implements UserService {
  private InventoryRepository inventoryRepository;
  private TransactionRepository transactionRepository;
  private UserRepository userRepository;
  private ItemRepository itemRepository;

  private PasswordEncoder passwordEncoder;
  private JwtTokenService jwtTokenService;

  @Override
  @Transactional
  public ResponseEntity<GetInfoResponse> getInfo(String token) {
    try {
      if (!jwtTokenService.validateToken(token)) {
        throw new UnauthorizedException("Invalid or expired JWT token");
      }

      String username = jwtTokenService.getUsernameFromToken(token);
      User user = userRepository.findByUsername(username).orElse(null);
      if (user == null) {
        throw new BadRequestException("User does not exists");
      }

      GetInfoResponse getInfoResponse = new GetInfoResponse();

      getInfoResponse.setCoins(user.getCoins());

      List<Inventory> itemsRaw = inventoryRepository.findAllByOwner(username);
      List<InventoryItem> items = new ArrayList<>();
      for (Inventory item : itemsRaw) {
        items.add(new InventoryItem(item));
      }
      getInfoResponse.setInventory(items);

      List<Transaction> fromRaw = transactionRepository.findAllBySender(username);
      List<CoinHistoryItemSent> from = new ArrayList<>();
      for (Transaction transaction : fromRaw) {
        from.add(new CoinHistoryItemSent(transaction));
      }
      List<Transaction> toRaw = transactionRepository.findAllByReceiver(username);
      List<CoinHistoryItemReceived> to = new ArrayList<>();
      for (Transaction transaction : toRaw) {
        to.add(new CoinHistoryItemReceived(transaction));
      }
      CoinHistoryResponse coinHistoryResponse = new CoinHistoryResponse();
      coinHistoryResponse.setSent(from);
      coinHistoryResponse.setReceived(to);
      getInfoResponse.setCoinHistory(coinHistoryResponse);

      return ResponseEntity.ok(getInfoResponse);
    } catch (UnauthorizedException e) {
      throw e;
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw new InternalServerErrorException("Internal server error: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public ResponseEntity<SendCoinResponse> sendCoin(SendCoinRequest sendCoinRequest, String token) {
    try {
      if (!jwtTokenService.validateToken(token)) {
        throw new UnauthorizedException("Invalid or expired JWT token");
      }
      String username = jwtTokenService.getUsernameFromToken(token);
      User userFrom = userRepository.findByUsername(username).orElse(null);
      if (userFrom == null) {
        throw new BadRequestException("toUser does not exists");
      }

      if (Objects.equals(userFrom.getUsername(), sendCoinRequest.getToUser())) {
        throw new BadRequestException("cannot send coins to yourself");
      }

      if (userFrom.getCoins() < sendCoinRequest.getAmount()) {
        throw new BadRequestException("amount not enough");
      }

      User userTo = userRepository.findByUsername(sendCoinRequest.getToUser()).orElse(null);
      if (userTo == null) {
        throw new BadRequestException("toUser does not exists");
      }

      userRepository.updateAmountByUser(
          userFrom.getUsername(), userFrom.getCoins() - sendCoinRequest.getAmount());
      userRepository.updateAmountByUser(
          userTo.getUsername(), userTo.getCoins() + sendCoinRequest.getAmount());

      Transaction transaction =
          transactionRepository.findBySenderAndReceiver(
              userFrom.getUsername(), userTo.getUsername());
      if (transaction == null) {
        transaction = new Transaction();
        transaction.setSender(userFrom.getUsername());
        transaction.setReceiver(userTo.getUsername());
        transaction.setAmount(sendCoinRequest.getAmount());
        transactionRepository.save(transaction);
      } else {
        transaction.setAmount(transaction.getAmount() + sendCoinRequest.getAmount());
        transactionRepository.updateAmountBySenderAndReceiver(
            transaction.getSender(), transaction.getReceiver(), transaction.getAmount());
      }
      return ResponseEntity.ok(null);
    } catch (UnauthorizedException e) {
      throw e;
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw new InternalServerErrorException("Internal server error: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public ResponseEntity<BuyItemResponse> buyItem(String itemName, String token) {
    try {
      if (!jwtTokenService.validateToken(token)) {
        throw new UnauthorizedException("Invalid or expired JWT token");
      }

      Item item = itemRepository.findByItem(itemName);
      if (item == null) {
        throw new BadRequestException("item does not exists");
      }

      String username = jwtTokenService.getUsernameFromToken(token);
      User user = userRepository.findByUsername(username).orElse(null);
      if (user == null) {
        throw new BadRequestException("User does not exists");
      }

      if (user.getCoins() < item.getPrice()) {
        throw new BadRequestException("amount not enough");
      }

      userRepository.updateAmountByUser(user.getUsername(), user.getCoins() - item.getPrice());
      Inventory inventory =
          inventoryRepository.findByOwnerAndItem(user.getUsername(), item.getItem());
      if (inventory == null) {
        inventory = new Inventory();
        inventory.setOwner(user.getUsername());
        inventory.setItem(item.getItem());
        inventory.setAmount(1);
        inventoryRepository.save(inventory);
      } else {
        inventoryRepository.updateAmountByOwnerAndItem(
            user.getUsername(), item.getItem(), inventory.getAmount() + 1);
      }

      return ResponseEntity.ok(null);
    } catch (UnauthorizedException e) {
      throw e;
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw new InternalServerErrorException("Internal server error: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public ResponseEntity<AuthResponse> authUser(AuthRequest authRequest) {
    try {
      User user = userRepository.findByUsername(authRequest.getUsername()).orElse(null);

      if (user == null) {
        user = new User();
        user.setUsername(authRequest.getUsername());
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        user.setCoins(1000);
        userRepository.save(user);
      }

      if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
        throw new BadRequestException("Invalid password");
      }

      AuthResponse authResponse = new AuthResponse();
      authResponse.setToken(jwtTokenService.generateToken(user.getUsername()));

      return ResponseEntity.ok().body(authResponse);
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw new InternalServerErrorException("Internal server error: " + e.getMessage());
    }
  }
}
