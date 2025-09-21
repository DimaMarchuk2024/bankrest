package com.example.bankcards.controller;

import com.example.bankcards.dto.PageResponse;
import com.example.bankcards.dto.TransferCreateEditDto;
import com.example.bankcards.dto.TransferReadDto;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.filter.TransferFilter;
import com.example.bankcards.service.TransferService;
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
public class TransferRestController {

    private final TransferService transferService;

    @GetMapping("/transfers")
    public PageResponse<TransferReadDto> findAll(TransferFilter transferFilter, Pageable pageable) {
        Page<TransferReadDto> page = transferService.findAll(transferFilter, pageable);

        return PageResponse.of(page);
    }

    @GetMapping("/users/{userId}/transfers")
    public Page<TransferReadDto> findAllByUserId(@PathVariable("userId") Long id, Pageable pageable) {
        return transferService.findAllByUserId(id, pageable);
    }

//    @GetMapping("/users/{userId}/transfers")
//    public Page<TransferReadDto> findAllByCardFrom(String cardFrom, Pageable pageable) {
//        return transferService.findAllByCardFrom(cardFrom, pageable);
//    }

    @GetMapping("/transfers/{id}")
    public TransferReadDto findById(@PathVariable("id") Long id) {
        return transferService.findById(id);
    }

    @PostMapping("/users/{userId}/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferReadDto create(@PathVariable("userId") Long userId,
                                  Long idCardFrom,
                                  Long idCardTo,
                                  BigDecimal sum,
                                  Pageable pageable,
                                  CardFilter cardFilter) {
        return transferService.create(idCardFrom, idCardTo, sum, userId, pageable, cardFilter);
    }

    @PutMapping("/transfers/{id}")
    public TransferReadDto update(@PathVariable("id") Long id, @RequestBody @Validated TransferCreateEditDto transferCreateEditDto) {
        return transferService.update(id, transferCreateEditDto);
    }

    @DeleteMapping("/transfers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        if (!transferService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
