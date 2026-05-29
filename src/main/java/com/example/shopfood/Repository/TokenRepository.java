package com.example.shopfood.Repository;

import com.example.shopfood.Model.Entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByToken(String token);

    List<Token> findAllByExpirationIsAfter(Date exDate);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.token = :token")
    void deleteByTokenValue(String token);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiration < CURRENT_TIMESTAMP")
    int deleteExpired();
}
