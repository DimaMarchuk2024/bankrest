package com.example.bankcards.controller;


import com.example.bankcards.dto.CardCreateEditDto;
import com.example.bankcards.dto.CardReadDto;
import com.example.bankcards.dto.PageResponse;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CardRestController {

    private final CardService cardService;

    @GetMapping("/cards")
    public PageResponse<CardReadDto> findAll(CardFilter cardFilter, Pageable pageable) {
        Page<CardReadDto> page = cardService.findAll(cardFilter, pageable);
        return PageResponse.of(page);
    }

    @GetMapping("/users/{userId}/cards")
    public Page<CardReadDto> findAllByUserId(@PathVariable("userId") Long id, CardFilter cardFilter, Pageable pageable) {
        return cardService.findAllByUserId(id, pageable, cardFilter);
    }

    @GetMapping("/cards/{id}")
    public CardReadDto findById(@PathVariable("id") Long id) {
        return cardService.findById(id);
    }

    @GetMapping("/cards/{id}/balance")
    public BigDecimal getBalance(@PathVariable("id") Long id) {
        return cardService.getBalance(id);
    }

    @PostMapping("/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public CardReadDto create(@RequestBody @Validated CardCreateEditDto cardCreateEditDto) {
        return cardService.create(cardCreateEditDto);
    }

    @PutMapping("/cards/{id}")
    public CardReadDto update(@PathVariable("id") Long id, @RequestBody @Validated CardCreateEditDto cardCreateEditDto) {
        return cardService.update(id, cardCreateEditDto);
    }

    @PutMapping("/cards/{id}/blocked")
    public CardReadDto blockingCard(@PathVariable("id") Long id) {
        return cardService.blockingCard(id);
    }

    @DeleteMapping("/cards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        if (!cardService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
