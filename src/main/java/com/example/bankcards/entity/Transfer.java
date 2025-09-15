package com.example.bankcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Transfer implements BaseEntity<Long>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "card_from")
    private String cardFrom;

    @Column(name = "card_to")
    private String cardTo;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    private BigDecimal sum;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(user.getId(), transfer.user.getId())
               && Objects.equals(cardFrom, transfer.cardFrom)
               && Objects.equals(cardTo, transfer.cardTo)
               && Objects.equals(transferDate, transfer.transferDate)
               && Objects.equals(sum, transfer.sum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId(), cardFrom, cardTo, transferDate, sum);
    }

    @Override
    public String toString() {
        return "Transfer{" +
               "userId=" + user.getId() +
               ", cardFrom='" + cardFrom + '\'' +
               ", cardTo='" + cardTo + '\'' +
               ", transferDate=" + transferDate +
               ", sum=" + sum +
               '}';
    }
}
