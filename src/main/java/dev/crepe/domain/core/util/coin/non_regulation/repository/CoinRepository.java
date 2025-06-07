package dev.crepe.domain.core.util.coin.non_regulation.repository;


import dev.crepe.domain.core.util.coin.non_regulation.model.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CoinRepository extends JpaRepository<Coin, Long> {

  Coin findByCurrency(String currency);
  boolean existsByCurrency(String currency);

  @Query("SELECT c.currency FROM Coin c")
  List<String> findAllCurrencies();

}
