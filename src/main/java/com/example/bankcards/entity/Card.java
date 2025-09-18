package com.example.bankcards.entity;

import com.example.bankcards.enumpack.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Card implements BaseEntity<Long>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String number;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JoinColumn(name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    private BigDecimal balance;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(number, card.number)
               && Objects.equals(user.getId(), card.user.getId())
               && Objects.equals(expirationDate, card.expirationDate)
               && status == card.status
               && Objects.equals(balance, card.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, user.getId(), expirationDate, status, balance);
    }

    @Override
    public String toString() {
        return "Card{" +
               "number='" + number + '\'' +
               ", userId=" + user.getId() +
               ", expirationDate=" + expirationDate +
               ", status=" + status +
               ", balance=" + balance +
               '}';
    }
}

