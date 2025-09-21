package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface TransferRepository extends JpaRepository<Transfer, Long>, QuerydslPredicateExecutor<Transfer> {

    /**
     * Находит все переводы по id пользователя,
     * упорядоченные по дате перевода в обратном порядке.
     **/
    @Query(value = "select t from Transfer t " +
                   "where t.user.id = :userId order by t.transferDate desc")
    Page<Transfer> findAllByUserId(Long userId, Pageable pageable);
}