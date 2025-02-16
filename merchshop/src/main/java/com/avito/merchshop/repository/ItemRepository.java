package com.avito.merchshop.repository;

import com.avito.merchshop.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
  Item findByItem(String item);
}
