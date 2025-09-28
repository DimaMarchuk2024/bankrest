package com.example.bankcards.service;

import com.example.bankcards.dto.TransferCreateEditDto;
import com.example.bankcards.dto.TransferReadDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.enumpack.Status;
import com.example.bankcards.filter.TransferFilter;
import com.example.bankcards.mapper.TransferCreateEditMapper;
import com.example.bankcards.mapper.TransferReadMapper;
import com.example.bankcards.predicate.QPredicate;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.Base64Codec;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.QTransfer.transfer;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final TransferReadMapper transferReadMapper;
    private final TransferCreateEditMapper transferCreateEditMapper;

    public Page<TransferReadDto> findAll(TransferFilter transferFilter, Pageable pageable) {
        Predicate predicate = QPredicate.builder()
                .add(transferFilter.getTransferDate(), transfer.transferDate::after)
                .add(transferFilter.getCardFrom(), transfer.cardFrom::contains)
                .add(transferFilter.getCardTo(), transfer.cardTo::contains)
                .buildAnd();

        return transferRepository.findAll(predicate, pageable)
                .map(transferReadMapper::map);
    }

    public TransferReadDto findById(Long id) {
        return transferRepository.findById(id)
                .map(transferReadMapper::map)
                .orElseThrow(() -> new EntityNotFoundException("Not found transfer with id = " + id));
    }

    public Page<TransferReadDto> findAllByUserId(Long userId, Pageable pageable) {
        return transferRepository.findAllByUserId(userId, pageable)
                .map(transferReadMapper::map);
    }

    @Transactional
    public TransferReadDto create(TransferCreateEditDto transferCreateEditDto) {
        String numberCardFrom = transferCreateEditDto.getCardFrom();
        String numberCardTo = transferCreateEditDto.getCardTo();
        BigDecimal sum = transferCreateEditDto.getSum();
        Long userId = transferCreateEditDto.getUserId();

        List<Card> cardsByUserId = cardRepository.findAllByUserId(userId);
        Card cardFrom = cardsByUserId.stream()
                .filter(card -> Base64Codec.decodeCardNumber(card.getNumber()).equals(numberCardFrom))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Not found card with number = " + numberCardFrom));
        Card cardTo = cardsByUserId.stream()
                .filter(card -> Base64Codec.decodeCardNumber(card.getNumber()).equals(numberCardTo))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Not found card with id = " + numberCardTo));

        if (numberCardFrom.equals(numberCardTo)) {
            throw new IllegalArgumentException("Select different accounts");
        } else if (cardFrom.getBalance().compareTo(sum) < 0) {
            throw new IllegalArgumentException("Insufficient funds on the card");
        } else if (cardFrom.getStatus().equals(Status.EXPIRED) || cardTo.getStatus().equals(Status.EXPIRED)) {
            throw new IllegalArgumentException("The card has expired");
        } else if (cardFrom.getStatus().equals(Status.BLOCKED) || cardTo.getStatus().equals(Status.BLOCKED)) {
            throw new IllegalArgumentException("Card is blocked");
        } else {
            cardFrom.setBalance(cardFrom.getBalance().subtract(sum));
            cardRepository.saveAndFlush(cardFrom);
            cardTo.setBalance(cardTo.getBalance().add(sum));
            cardRepository.saveAndFlush(cardTo);

            return Optional.of(transferCreateEditDto)
                    .map(transferCreateEditMapper::map)
                    .map(transferRepository::save)
                    .map(transferReadMapper::map)
                    .orElseThrow();
        }
    }

    @Transactional
    public TransferReadDto update(Long id, TransferCreateEditDto transferCreateEditDto) {
        Transfer transferForUpdate = transferRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found transfer with id = " + id));

        return Optional.of(transferForUpdate)
                .map(transfer -> transferCreateEditMapper.map(transferCreateEditDto, transfer))
                .map(transferRepository::saveAndFlush)
                .map(transferReadMapper::map)
                .orElseThrow(() -> new IllegalArgumentException("Failed to update the transfer with Id = " + id));
    }

    @Transactional
    public boolean delete(Long id) {
        return Optional.ofNullable(transferRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Not found transfer with id = " + id)))
                .map(transfer -> {
                    transferRepository.delete(transfer);
                    transferRepository.flush();
                    log.info("Transfer with id = " + id + " deleted");
                    return true;
                })
                .orElse(false);
    }
}
