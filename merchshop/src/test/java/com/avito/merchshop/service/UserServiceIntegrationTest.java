package com.avito.merchshop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avito.merchshop.domain.Inventory;
import com.avito.merchshop.domain.Item;
import com.avito.merchshop.domain.Transaction;
import com.avito.merchshop.domain.User;
import com.avito.merchshop.jwt.JwtTokenService;
import com.avito.merchshop.model.request.SendCoinRequest;
import com.avito.merchshop.repository.InventoryRepository;
import com.avito.merchshop.repository.ItemRepository;
import com.avito.merchshop.repository.TransactionRepository;
import com.avito.merchshop.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class UserServiceIntegrationTest {

  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:11.1");

  static {
    postgreSQLContainer.start();
    // Устанавливаем параметры подключения через System.setProperty
    System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
    System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    System.setProperty(
        "spring.datasource.driver-class-name", postgreSQLContainer.getDriverClassName());
  }

  @AfterAll
  static void tearDown() {
    postgreSQLContainer.stop();
  }

  @Autowired private EntityManager entityManager;

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;

  @Autowired private InventoryRepository inventoryRepository;

  @Autowired private TransactionRepository transactionRepository;

  @Autowired private ItemRepository itemRepository;

  @Autowired private JwtTokenService jwtTokenService;

  @Autowired private PasswordEncoder passwordEncoder;

  private String token;
  private final String TEST_USER = "testUser";

  @BeforeEach
  void setUp() {
    // Создаем тестового пользователя
    User user = new User();
    user.setUsername(TEST_USER);
    user.setPassword(passwordEncoder.encode("password123"));
    user.setCoins(1000); // 1000 монет
    userRepository.save(user);

    // Создаем тестовый товар
    Item item = new Item();
    item.setItem("t-shirt");
    item.setPrice(80); // Цена 80 монет
    if (itemRepository.findByItem(item.getItem()) == null) {
      itemRepository.save(item);
    }

    // Генерируем JWT токен
    token = jwtTokenService.generateToken(TEST_USER);
  }

  @Test
  void testBuyItem_Success() throws Exception {
    mockMvc
        .perform(get("/api/buy/{item}", "t-shirt").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    entityManager.clear();

    // Проверяем, что товар добавлен в инвентарь
    Inventory inventory = inventoryRepository.findByOwnerAndItem(TEST_USER, "t-shirt");
    assertNotNull(inventory);
    assertEquals(1, inventory.getAmount());

    // Проверяем, что монеты списаны
    User user = userRepository.findByUsername(TEST_USER).orElse(null);
    assertNotNull(user);
    assertEquals(920, user.getCoins()); // Было 1000, купили за 80
  }

  @Test
  void testSendCoin_Success() throws Exception {
    // Создаем второго пользователя
    User recipient = new User();
    recipient.setUsername("recipientUser");
    recipient.setPassword(passwordEncoder.encode("password456"));
    recipient.setCoins(1000);
    userRepository.save(recipient);

    // Отправляем запрос на перевод монет
    SendCoinRequest request = new SendCoinRequest();
    request.setToUser("recipientUser");
    request.setAmount(150);

    mockMvc
        .perform(
            post("/api/sendCoin")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isOk());

    entityManager.clear();

    // Проверяем баланс отправителя
    User sender = userRepository.findByUsername(TEST_USER).orElseThrow();
    assertEquals(850, sender.getCoins()); // Было 1000, отправили 150

    // Проверяем баланс получателя
    User updatedRecipient = userRepository.findByUsername("recipientUser").orElseThrow();
    assertEquals(1150, updatedRecipient.getCoins()); // Было 1000, получили 150

    // Проверяем, что транзакция сохранена
    Transaction transaction =
        transactionRepository.findBySenderAndReceiver(TEST_USER, "recipientUser");
    assertNotNull(transaction);
    assertEquals(150, transaction.getAmount());
  }
}
