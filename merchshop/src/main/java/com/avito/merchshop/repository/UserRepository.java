package com.avito.merchshop.repository;

import com.avito.merchshop.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
  Optional<User> findByUsername(String username);

  @Modifying
  @Query("UPDATE User u SET u.coins = :coins WHERE u.username = :username")
  void updateAmountByUser(@Param("username") String username, @Param("coins") int coins);
}
