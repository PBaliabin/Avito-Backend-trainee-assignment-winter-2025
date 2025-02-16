package com.avito.merchshop.repository;

import com.avito.merchshop.domain.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
  List<Transaction> findAllBySender(String from);

  List<Transaction> findAllByReceiver(String to);

  Transaction findBySenderAndReceiver(String sender, String receiver);

  @Modifying
  @Query(
      "UPDATE Transaction t SET t.amount = :amount WHERE t.sender = :sender AND t.receiver = :receiver")
  void updateAmountBySenderAndReceiver(
      @Param("sender") String sender,
      @Param("receiver") String receiver,
      @Param("amount") int amount);
}
