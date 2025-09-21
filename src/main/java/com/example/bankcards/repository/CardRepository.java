package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long>, QuerydslPredicateExecutor<Card> {

    /**
     * Находит все карты по id пользователя,
     * упорядоченные по дате окончания срока в обратном порядке.
     **/
    @Query(value = "select c from Card c " +
                   "where c.user.id = :userId order by c.expirationDate desc")
    Page<Card> findAllByUserId(Long userId, Pageable pageable, Predicate predicate);

}
