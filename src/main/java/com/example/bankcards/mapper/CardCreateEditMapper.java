package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardCreateEditDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Base64Codec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CardCreateEditMapper implements Mapper<CardCreateEditDto, Card> {

    private final UserRepository userRepository;

    @Override
    public Card map(CardCreateEditDto cardCreateEditDto) {
        Card card = new Card();
        copy(cardCreateEditDto, card);
        return card;
    }

    @Override
    public Card map(CardCreateEditDto cardCreateEditDto, Card card) {
        copy(cardCreateEditDto, card);
        return card;
    }

    private void copy(CardCreateEditDto cardCreateEditDto, Card card) {
        card.setNumber(Base64Codec.encodeCardNumber(cardCreateEditDto.getNumber()));
        card.setUser(getUser(cardCreateEditDto.getUserId()));
        card.setExpirationDate(cardCreateEditDto.getExpirationDate());
        card.setStatus(cardCreateEditDto.getStatus());
        card.setBalance(cardCreateEditDto.getBalance());
    }

    private User getUser(Long userId) {
        return Optional.ofNullable(userId)
                .flatMap(userRepository::findById)
                .orElseThrow();
    }
}
