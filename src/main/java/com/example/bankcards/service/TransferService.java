package com.example.bankcards.service;

import com.example.bankcards.dto.TransferCreateEditDto;
import com.example.bankcards.dto.TransferReadDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.enumpack.Status;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.filter.TransferFilter;
import com.example.bankcards.mapper.TransferCreateEditMapper;
import com.example.bankcards.mapper.TransferReadMapper;
import com.example.bankcards.predicate.QPredicate;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.QCard.card;
import static com.example.bankcards.entity.QTransfer.transfer;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
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
    public TransferReadDto create(Long idCardFrom,
                                  Long idCardTo,
                                  BigDecimal sum,
                                  Long userId) {
        List<Card> cardsByUserId = cardRepository.findAllByUserId(userId);

        Card cardFrom = cardsByUserId.stream()
                .filter(card -> card.getId().equals(idCardFrom))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Not found card with id = " + idCardFrom));

        Card cardTo = cardsByUserId.stream()
                .filter(card -> card.getId().equals(idCardTo))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Not found card with id = " + idCardTo));

        if (idCardFrom.equals(idCardTo)) {
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

            Transfer transfer = Transfer.builder()
                    .user(userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("Not found user with id = " + userId)))
                    .cardFrom(cardFrom.getNumber())
                    .cardTo(cardTo.getNumber())
                    .transferDate(LocalDate.now())
                    .sum(sum)
                    .build();
            Transfer transferResult = transferRepository.saveAndFlush(transfer);
            log.info("Transfer was completed successfully");
            return transferReadMapper.map(transferResult);
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
