package com.avito.merchshop.repository;

import com.avito.merchshop.domain.Inventory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
  List<Inventory> findAllByOwner(String owner);

  Inventory findByOwnerAndItem(String owner, String item);

  @Modifying
  @Query("UPDATE Inventory i SET i.amount = :amount WHERE i.owner = :owner AND i.item = :item")
  void updateAmountByOwnerAndItem(
      @Param("owner") String owner, @Param("item") String item, @Param("amount") int amount);
}
