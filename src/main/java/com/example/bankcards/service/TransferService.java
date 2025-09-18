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
import com.example.bankcards.repository.UserRepository;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.QTransfer.transfer;


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
                .add(transferFilter.getCardFromDto(), transfer.cardFrom::contains)
                .add(transferFilter.getCardToDto(), transfer.cardTo::contains)
                .buildAnd();

        return transferRepository.findAll(predicate, pageable)
                .map(transferReadMapper::map);
    }

    public Optional<TransferReadDto> findById(Long id) {
        return transferRepository.findById(id)
                .map(transferReadMapper::map);
    }

    public Page<TransferReadDto> findAllByUserId(Long userId, Pageable pageable) {
        return transferRepository.findAllByUserId(userId, pageable)
                .map(transferReadMapper::map);
    }

    public Page<TransferReadDto> findAllByCardFrom(String cardFrom, Pageable pageable) {
        return transferRepository.findAllByCardFrom(cardFrom, pageable)
                .map(transferReadMapper::map);
    }

    @Transactional
    public TransferReadDto create(Long idCardFrom, Long idCardTo, BigDecimal sum, Long userId) {

        List<Card> cardsByUserId = cardRepository.findAllByUserId(userId);

        Card cardFrom = cardsByUserId.stream()
                .filter(card -> card.getId().equals(idCardFrom))
                .findFirst()
                .orElseThrow();

        Card cardTo = cardsByUserId.stream()
                .filter(card -> card.getId().equals(idCardTo))
                .findFirst()
                .orElseThrow();

        if (idCardFrom.equals(idCardTo)) {
            throw new TransactionUsageException("Select different accounts");
        } else if (cardFrom.getBalance().compareTo(sum) < 0) {
            throw new TransactionUsageException("Insufficient funds on the card");
        } else if (cardFrom.getStatus().equals(Status.EXPIRED) || cardTo.getStatus().equals(Status.EXPIRED)) {
            throw new TransactionUsageException("The card has expired");
        } else if (cardFrom.getStatus().equals(Status.BLOCKED) || cardTo.getStatus().equals(Status.BLOCKED)) {
            throw new TransactionUsageException("Card is blocked");
        } else {
            cardFrom.setBalance(cardFrom.getBalance().subtract(sum));
            cardRepository.saveAndFlush(cardFrom);
            cardTo.setBalance(cardTo.getBalance().add(sum));
            cardRepository.saveAndFlush(cardTo);

            Transfer transfer = Transfer.builder()
                    .user(userRepository.findById(userId).orElseThrow())
                    .cardFrom(cardFrom.getNumber())
                    .cardTo(cardTo.getNumber())
                    .transferDate(LocalDate.now())
                    .sum(sum)
                    .build();
            Transfer transferResult = transferRepository.saveAndFlush(transfer);

            return Optional.of(transferResult)
                    .map(transferReadMapper::map)
                    .orElseThrow();
        }
    }

    @Transactional
    public Optional<TransferReadDto> update(Long id, TransferCreateEditDto transferCreateEditDto) {
        return transferRepository.findById(id)
                .map(transfer -> transferCreateEditMapper.map(transferCreateEditDto, transfer))
                .map(transferRepository::saveAndFlush)
                .map(transferReadMapper::map);
    }

    @Transactional
    public boolean delete(Long id) {
        return transferRepository.findById(id)
                .map(transfer -> {
                    transferRepository.delete(transfer);
                    transferRepository.flush();
                    return true;
                })
                .orElse(false);
    }
}
