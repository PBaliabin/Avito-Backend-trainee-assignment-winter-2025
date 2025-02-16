package com.avito.merchshop.config;

import com.avito.merchshop.domain.Item;
import com.avito.merchshop.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class DatabaseInitializer {

    private ItemRepository itemRepository;

    @PostConstruct
    public void init() {
        if (itemRepository.count() > 0) {
            return;
        }
        List<String> names = List.of(
                "t-shirt", "cup", "book", "pen", "powerbank",
                "hoody", "umbrella", "socks", "wallet", "pink-hoody");
        List<Integer> prices = List.of(
                80, 20, 50, 10, 200,
                300, 200, 10, 50, 500
        );
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            Item item = new Item();
            item.setItem(names.get(i));
            item.setPrice(prices.get(i));
            items.add(item);
        }
        itemRepository.saveAll(items);
    }
}

