package com.example.bankcards.mapper;


import com.example.bankcards.dto.CardReadDto;
import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.BankCardMask;
import com.example.bankcards.util.Base64Codec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CardReadMapper implements Mapper<Card, CardReadDto> {

    private final UserReadMapper userReadMapper;

    @Override
    public CardReadDto map(Card card) {

        UserReadDto userReadDto = Optional.ofNullable(card.getUser())
                .map(userReadMapper::map)
                .orElseThrow();

        return new CardReadDto(
                card.getId(),
                BankCardMask.getNumberCardMask(Base64Codec.decodeCardNumber(card.getNumber())),
                userReadDto,
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()

        );
    }
}