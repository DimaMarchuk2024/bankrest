package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateEditDto;
import com.example.bankcards.dto.CardReadDto;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.mapper.CardCreateEditMapper;
import com.example.bankcards.mapper.CardReadMapper;
import com.example.bankcards.predicate.QPredicate;
import com.example.bankcards.repository.CardRepository;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.QCard.card;

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

    public Optional<CardReadDto> findById(Long id) {
        return cardRepository.findById(id)
                .map(cardReadMapper::map);
    }

    public List<CardReadDto> findAllByUserId(Long userId) {
        return cardRepository.findAllByUserId(userId).stream()
                .map(cardReadMapper::map)
                .toList();
    }

    @Transactional
    public CardReadDto create(CardCreateEditDto cardCreateEditDto) {
        return Optional.of(cardCreateEditDto)
                .map(cardCreateEditMapper::map)
                .map(cardRepository::save)
                .map(cardReadMapper::map)
                .orElseThrow();
    }

    @Transactional
    public Optional<CardReadDto> update(Long id, CardCreateEditDto cardCreateEditDto) {
        return cardRepository.findById(id)
                .map(user -> cardCreateEditMapper.map(cardCreateEditDto, user))
                .map(cardRepository::saveAndFlush)
                .map(cardReadMapper::map);
    }

    @Transactional
    public boolean delete(Long id) {
        return cardRepository.findById(id)
                .map(user -> {
                    cardRepository.delete(user);
                    cardRepository.flush();
                    return true;
                })
                .orElse(false);
    }
}
