package com.example.bankcards.controller;

import com.example.bankcards.dto.PageResponse;
import com.example.bankcards.dto.TransferCreateEditDto;
import com.example.bankcards.dto.TransferReadDto;
import com.example.bankcards.filter.TransferFilter;
import com.example.bankcards.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TransferRestController {

    private final TransferService transferService;

    @GetMapping("/transfers")
    public PageResponse<TransferReadDto> findAll(TransferFilter transferFilter,
                                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransferReadDto> pageResult = transferService.findAll(transferFilter, pageable);
        return PageResponse.of(pageResult);
    }

    @GetMapping("/users/{userId}/transfers")
    public PageResponse<TransferReadDto> findAllByUserId(@PathVariable("userId") Long id,
                                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransferReadDto> pageResult = transferService.findAllByUserId(id, pageable);
        return PageResponse.of(pageResult);
    }

    @GetMapping("/transfers/{id}")
    public TransferReadDto findById(@PathVariable("id") Long id) {
        return transferService.findById(id);
    }

    @PostMapping("/users/{userId}/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferReadDto create(@RequestBody @Validated TransferCreateEditDto transferCreateEditDto) {
        return transferService.create(transferCreateEditDto);
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
