package com.avito.merchshop.service;

import com.avito.merchshop.domain.Inventory;
import com.avito.merchshop.domain.Item;
import com.avito.merchshop.domain.Transaction;
import com.avito.merchshop.domain.User;
import com.avito.merchshop.exception.BadRequestException;
import com.avito.merchshop.exception.UnauthorizedException;
import com.avito.merchshop.jwt.JwtTokenService;
import com.avito.merchshop.model.request.AuthRequest;
import com.avito.merchshop.model.request.SendCoinRequest;
import com.avito.merchshop.model.response.AuthResponse;
import com.avito.merchshop.model.response.BuyItemResponse;
import com.avito.merchshop.model.response.GetInfoResponse;
import com.avito.merchshop.repository.InventoryRepository;
import com.avito.merchshop.repository.ItemRepository;
import com.avito.merchshop.repository.TransactionRepository;
import com.avito.merchshop.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest // Загружает Spring-контекст (реальные бины)
@Testcontainers
class UserServiceImplTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:11.1");

    static {
        postgreSQLContainer.start();
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
        System.setProperty("spring.datasource.driver-class-name", postgreSQLContainer.getDriverClassName());
    }

    @AfterAll
    static void tearDown() {
        postgreSQLContainer.stop();
    }

    @Autowired
    private PasswordEncoder passwordEncoder; // Используем настоящий бин

    @MockBean
    private JwtTokenService jwtTokenService; // Настоящий Jwt сервис

    @MockBean
    private UserRepository userRepository; // Мок базы данных

    @MockBean
    private InventoryRepository inventoryRepository; // Мок инвентаря

    @MockBean
    private TransactionRepository transactionRepository; // Мок транзакций

    @MockBean
    private ItemRepository itemRepository; // Мок товаров

    @Autowired
    private UserServiceImpl userService; // Внедряем настоящий сервис

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testUser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setCoins(1000);
    }

    @Test
    void getInfo_ValidToken_ReturnsUserInfo() {
        String token = "validToken";

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.getUsernameFromToken(token)).thenReturn(user.getUsername());

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        when(inventoryRepository.findAllByOwner(user.getUsername())).thenReturn(List.of());

        when(transactionRepository.findAllBySender(user.getUsername())).thenReturn(List.of());
        when(transactionRepository.findAllByReceiver(user.getUsername())).thenReturn(List.of());

        ResponseEntity<GetInfoResponse> response = userService.getInfo(token);

        assertNotNull(response.getBody());
        assertEquals(1000, response.getBody().getCoins());
        assertTrue(response.getBody().getInventory().isEmpty());
        assertNotNull(response.getBody().getCoinHistory());
        assertTrue(response.getBody().getCoinHistory().getSent().isEmpty());
        assertTrue(response.getBody().getCoinHistory().getReceived().isEmpty());

        verify(userRepository, times(1)).findByUsername(user.getUsername());
    }

    @Test
    void getInfo_InvalidToken_ThrowsUnauthorizedException() {
        String invalidToken = "invalidToken";

        when(jwtTokenService.validateToken(invalidToken)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.getInfo(invalidToken));
    }

    @Test
    void sendCoin_SuccessfulTransaction() {
        String token = "correctToken";
        User receiver = new User();
        receiver.setUsername("receiverUser");
        receiver.setCoins(500);

        SendCoinRequest request = new SendCoinRequest();
        request.setToUser("receiverUser");
        request.setAmount(300);

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.getUsernameFromToken(token)).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("receiverUser")).thenReturn(Optional.of(receiver));
        when(transactionRepository.findBySenderAndReceiver(anyString(), anyString())).thenReturn(null);

        ResponseEntity<?> response = userService.sendCoin(request, token);

        assertNotNull(response);
        verify(userRepository).updateAmountByUser(user.getUsername(), 700);
        verify(userRepository).updateAmountByUser("receiverUser", 800);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void sendCoin_InsufficientFunds_ThrowsBadRequestException() {
        String token = "correctToken";

        SendCoinRequest request = new SendCoinRequest();
        request.setToUser("receiverUser");
        request.setAmount(2000); // Больше, чем у пользователя

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.getUsernameFromToken(token)).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.sendCoin(request, token));
    }

    @Test
    void buyItem_SuccessfulPurchase() {
        String token = "correctToken";
        Item item = new Item();
        item.setItem("pen");
        item.setPrice(10);

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.getUsernameFromToken(token)).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(itemRepository.findByItem(item.getItem())).thenReturn(item);
        when(inventoryRepository.findByOwnerAndItem(user.getUsername(), item.getItem())).thenReturn(null);

        ResponseEntity<BuyItemResponse> response = userService.buyItem(item.getItem(), token);

        assertNotNull(response);
        verify(userRepository).updateAmountByUser(user.getUsername(), user.getCoins() - 10);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void buyItem_InsufficientFunds_ThrowsBadRequestException() {
        String token = "correctToken";
        Item expensiveItem = new Item();
        expensiveItem.setItem("cup");
        expensiveItem.setPrice(20);
        user.setCoins(0);

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.getUsernameFromToken(token)).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(itemRepository.findByItem(expensiveItem.getItem())).thenReturn(expensiveItem);

        assertThrows(BadRequestException.class, () -> userService.buyItem(expensiveItem.getItem(), token));
    }

    @Test
    void buyItem_WrongItem_ThrowsBadRequestException() {
        String token = "correctToken";
        Item expensiveItem = new Item();
        expensiveItem.setItem("plane");
        expensiveItem.setPrice(20);

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(jwtTokenService.getUsernameFromToken(token)).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(itemRepository.findByItem(expensiveItem.getItem())).thenReturn(null);

        assertThrows(BadRequestException.class, () -> userService.buyItem(expensiveItem.getItem(), token));
    }


    @Test
    void authUser_NewUser_CreatesUser() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("newUser");
        authRequest.setPassword("newPassword");

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateToken("newUser")).thenReturn("newToken");

        ResponseEntity<AuthResponse> response = userService.authUser(authRequest);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void authUser_ExistingUser_ValidPassword() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername(user.getUsername());
        authRequest.setPassword("password123");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(jwtTokenService.generateToken(user.getUsername())).thenReturn("newToken");

        ResponseEntity<AuthResponse> response = userService.authUser(authRequest);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authUser_ExistingUser_InvalidPassword_ThrowsException() {
        User existingUser = new User();
        existingUser.setUsername("testUser");
        existingUser.setPassword(passwordEncoder.encode("correctPassword"));

        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("testUser");
        authRequest.setPassword("wrongPassword");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(existingUser));

        assertThrows(BadRequestException.class, () -> userService.authUser(authRequest));

        verify(userRepository, never()).save(any(User.class));
    }
}
