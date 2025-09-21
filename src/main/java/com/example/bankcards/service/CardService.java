package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateEditDto;
import com.example.bankcards.dto.CardReadDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.enumpack.Status;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.mapper.CardCreateEditMapper;
import com.example.bankcards.mapper.CardReadMapper;
import com.example.bankcards.predicate.QPredicate;
import com.example.bankcards.repository.CardRepository;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static com.example.bankcards.entity.QCard.card;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final CardReadMapper cardReadMapper;
    private final CardCreateEditMapper cardCreateEditMapper;

    public Page<CardReadDto> findAll(CardFilter cardFilter, Pageable pageable) {
        Predicate predicate = QPredicate.builder()
                .add(cardFilter.getNumber(), card.number::contains)
                .add(cardFilter.getExpirationDate(), card.expirationDate::before)
                .buildAnd();

        return cardRepository.findAll(predicate, pageable)
                .map(cardReadMapper::map);
    }

    public CardReadDto findById(Long id) {
        return cardRepository.findById(id)
                .map(cardReadMapper::map)
                .orElseThrow(() -> new EntityNotFoundException("Not found card with id = " + id));
    }

    public BigDecimal getBalance(Long id) {
        return cardRepository.findById(id)
                .map(Card::getBalance)
                .orElseThrow(() -> new EntityNotFoundException("Not found card with id = " + id));
    }

    public Page<CardReadDto> findAllByUserId(Long userId, Pageable pageable, CardFilter cardFilter) {
        Predicate predicate = QPredicate.builder()
                .add(cardFilter.getNumber(), card.number::contains)
                .add(cardFilter.getExpirationDate(), card.expirationDate::before)
                .buildAnd();

        return cardRepository.findAllByUserId(userId, pageable, predicate)
                .map(cardReadMapper::map);
    }

    @Transactional
    public CardReadDto create(CardCreateEditDto cardCreateEditDto) {
        return Optional.of(cardCreateEditDto)
                .map(cardCreateEditMapper::map)
                .map(cardRepository::save)
                .map(cardReadMapper::map)
                .orElseThrow(() -> new IllegalArgumentException("Failed to create card"));
    }

    @Transactional
    public CardReadDto update(Long id, CardCreateEditDto cardCreateEditDto) {
        Card cardForUpdate = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found card with id = " + id));

        return Optional.of(cardForUpdate)
                .map(user -> cardCreateEditMapper.map(cardCreateEditDto, user))
                .map(cardRepository::saveAndFlush)
                .map(cardReadMapper::map)
                .orElseThrow(() -> new IllegalArgumentException("Failed to update the card with Id = " + id));
    }

    @Transactional
    public CardReadDto blockingCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found user with id = " + id));
        if (card.getStatus().equals(Status.BLOCKED)) {
            throw new IllegalArgumentException("Your card has already been blocked");
        }
        if (card.getStatus().equals(Status.EXPIRED)) {
            throw new IllegalArgumentException("Your card cannot be blocked because it has expired");
        }
        card.setStatus(Status.BLOCKED);
        return Optional.of(cardRepository.saveAndFlush(card))
                .map(cardReadMapper::map)
                .orElseThrow(() -> new RuntimeException("Failed to blocked the card with Id = " + id));
    }

    @Transactional
    public boolean delete(Long id) {
        return Optional.ofNullable(cardRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Not found card with id = " + id)))
                .map(card -> {
                    cardRepository.delete(card);
                    cardRepository.flush();
                    log.info("Card with id = " + id + " deleted");
                    return true;
                })
                .orElse(false);
    }
}
