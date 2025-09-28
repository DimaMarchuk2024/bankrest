package com.example.bankcards.service;

import com.example.bankcards.dto.TransferCreateEditDto;
import com.example.bankcards.dto.TransferReadDto;
import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.enumpack.Role;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.QTransfer.transfer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferReadMapper transferReadMapper;

    @Mock
    private TransferCreateEditMapper transferCreateEditMapper;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferService transferService;

    @Test
    void findAll() {
        TransferFilter filter = TransferFilter.builder().build();
        PageRequest pageable = PageRequest.of(0, 20);
        Transfer transfer = getTransfer();
        TransferReadDto transferReadDto = getTransferReadDto();
        Page<Transfer> pageTransfer = getPageTransfer();
        Predicate predicate = getPredicate(filter);
        doReturn(pageTransfer).when(transferRepository).findAll(predicate, pageable);
        doReturn(transferReadDto).when(transferReadMapper).map(transfer);

        Page<TransferReadDto> actualResult = transferService.findAll(filter, pageable);

        assertThat(actualResult).hasSize(2);
        verifyNoInteractions(transferCreateEditMapper);
    }

    @Test
    void findByIdSuccess() {
        Transfer transfer = getTransfer();
        TransferReadDto transferReadDto = getTransferReadDto();
        Optional<Transfer> optionalTransfer = Optional.of(transfer);
        Optional<TransferReadDto> optionalTransferReadDto = Optional.of(transferReadDto);
        doReturn(optionalTransfer).when(transferRepository).findById(transfer.getId());
        doReturn(optionalTransferReadDto.get()).when(transferReadMapper).map(optionalTransfer.get());

        TransferReadDto actualResult = transferService.findById(transfer.getId());

        verifyNoInteractions(transferCreateEditMapper);
        assertThat(actualResult).isEqualTo(transferReadDto);
        verify(transferRepository).findById(transfer.getId());
    }

    @Test
    void findByIdFailedIfTransferNotFound() {
        doReturn(Optional.empty()).when(transferRepository).findById(any());

        verifyNoInteractions(transferCreateEditMapper, transferReadMapper);
        assertThrows(EntityNotFoundException.class, () -> transferService.findById(any()));
    }

    @Test
    void findAllByUserIdSuccess() {
        User user = getUser();
        PageRequest pageable = PageRequest.of(0, 20);
        Transfer transfer = getTransfer();
        TransferReadDto transferReadDto = getTransferReadDto();
        Page<Transfer> pageTransfer = getPageTransfer();
        doReturn(pageTransfer).when(transferRepository).findAllByUserId(user.getId(), pageable);
        doReturn(transferReadDto).when(transferReadMapper).map(transfer);

        Page<TransferReadDto> actualResult = transferService.findAllByUserId(user.getId(), pageable);

        assertThat(actualResult).hasSize(2);
        verifyNoInteractions(transferCreateEditMapper);
    }

    @Test
    void findAllByUserIdIfUserCardsNotFound() {
        PageRequest pageable = PageRequest.of(0, 20);
        User user = getUser();
        doReturn(Page.empty()).when(transferRepository).findAllByUserId(user.getId(), pageable);

        Page<TransferReadDto> actualResult = transferService.findAllByUserId(user.getId(), pageable);

        assertThat(actualResult).isEmpty();
        verifyNoInteractions(transferCreateEditMapper);
    }

    @Test
    void createSuccess() {
        User user = getUser();
        TransferCreateEditDto transferCreateEditDto = getTransferCreateEditDto();
        TransferReadDto transferReadDto = getTransferReadDto();
        List<Card> cardList = getListCard();
        Transfer transfer = getTransfer();
        Card cardFrom = cardList.get(0);
        Card cardTo = cardList.get(1);
        doReturn(cardList).when(cardRepository).findAllByUserId(user.getId());
        doReturn(cardFrom).when(cardRepository).saveAndFlush(cardFrom);
        doReturn(cardTo).when(cardRepository).saveAndFlush(cardTo);
        doReturn(transfer).when(transferCreateEditMapper).map(transferCreateEditDto);
        doReturn(transfer).when(transferRepository).save(transfer);
        doReturn(transferReadDto).when(transferReadMapper).map(transfer);

        TransferReadDto actualResult = transferService.create(transferCreateEditDto);

        assertThat(actualResult.getCardFrom()).isEqualTo(transferReadDto.getCardFrom());
        assertThat(actualResult.getCardTo()).isEqualTo(transferReadDto.getCardTo());
        assertThat(actualResult.getId()).isEqualTo(transferReadDto.getId());
        verify(cardRepository).findAllByUserId(user.getId());
        verify(cardRepository).saveAndFlush(cardFrom);
        verify(cardRepository).saveAndFlush(cardTo);
        verify(transferRepository).save(transfer);
    }

    @Test
    void createFailedIfUserCardsNotFound() {
        User user = getUser();
        TransferCreateEditDto transferCreateEditDto = getTransferCreateEditDto();
        List<Card> cardList = new ArrayList<>();
        doReturn(cardList).when(cardRepository).findAllByUserId(user.getId());

        assertThrows(EntityNotFoundException.class, () -> transferService.create(transferCreateEditDto));
        verifyNoInteractions(transferReadMapper, transferRepository);
    }

    @Test
    void createFailedIfNumberCardFromEqualsNumberCardTo() {
        User user = getUser();
        Card card = getCard();
        List<Card> cardList = new ArrayList<>();
        cardList.add(card);
        cardList.add(card);
        doReturn(cardList).when(cardRepository).findAllByUserId(user.getId());
        TransferCreateEditDto transferCreateEditDto = TransferCreateEditDto.builder()
                .userId(user.getId())
                .cardFrom("1234123412341234")
                .cardTo("1234123412341234")
                .sum(BigDecimal.valueOf(50))
                .build();

        assertThrows(IllegalArgumentException.class, () -> transferService.create(transferCreateEditDto));
        verifyNoInteractions(transferReadMapper, transferRepository);
        verify(cardRepository).findAllByUserId(user.getId());
    }

    @Test
    void createFailedIfBalanceCardFromLessTransferSum() {
        User user = getUser();
        List<Card> cardList = getListCard();
        doReturn(cardList).when(cardRepository).findAllByUserId(user.getId());
        TransferCreateEditDto transferCreateEditDto = TransferCreateEditDto.builder()
                .userId(user.getId())
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .sum(BigDecimal.valueOf(1000.00))
                .build();

        assertThrows(IllegalArgumentException.class, () -> transferService.create(transferCreateEditDto));
        verifyNoInteractions(transferReadMapper, transferRepository);
        verify(cardRepository).findAllByUserId(user.getId());
    }

    @Test
    void createFailedIfCardIsBlocked() {
        User user = getUser();
        TransferCreateEditDto transferCreateEditDto = getTransferCreateEditDto();
        List<Card> cardList = getListCard();
        doReturn(cardList).when(cardRepository).findAllByUserId(user.getId());
        Card cardFrom = cardList.get(0);
        cardFrom.setStatus(Status.BLOCKED);

        assertThrows(IllegalArgumentException.class, () -> transferService.create(transferCreateEditDto));
        verifyNoInteractions(transferReadMapper, transferRepository);
        verify(cardRepository).findAllByUserId(user.getId());
    }

    @Test
    void createFailedIfCardIsExpired() {
        User user = getUser();
        TransferCreateEditDto transferCreateEditDto = getTransferCreateEditDto();
        List<Card> cardList = getListCard();
        doReturn(cardList).when(cardRepository).findAllByUserId(user.getId());
        Card cardFrom = cardList.get(0);
        cardFrom.setStatus(Status.EXPIRED);

        assertThrows(IllegalArgumentException.class, () -> transferService.create(transferCreateEditDto));
        verifyNoInteractions(transferReadMapper, transferRepository);
        verify(cardRepository).findAllByUserId(user.getId());
    }

    @Test
    void updateSuccess() {
        Transfer transfer = getTransfer();
        TransferReadDto transferReadDto = getTransferReadDto();
        TransferCreateEditDto transferCreateEditDto = getTransferCreateEditDto();
        doReturn(Optional.of(transfer)).when(transferRepository).findById(transfer.getId());
        doReturn(transfer).when(transferCreateEditMapper).map(transferCreateEditDto, transfer);
        doReturn(transfer).when(transferRepository).saveAndFlush(transfer);
        doReturn(transferReadDto).when(transferReadMapper).map(transfer);

        TransferReadDto actualResult = transferService.update(transfer.getId(), transferCreateEditDto);

        assertThat(actualResult).isEqualTo(transferReadDto);
        verify(transferRepository).saveAndFlush(transfer);
        verify(transferRepository).findById(transfer.getId());
    }

    @Test
    void updateFailedIfTransferNotFound() {
        doThrow(EntityNotFoundException.class).when(transferRepository).findById(any());

        assertThrows(EntityNotFoundException.class, () -> transferService.update(any(), getTransferCreateEditDto()));
        verifyNoInteractions(transferReadMapper, transferCreateEditMapper);
    }

    @Test
    void updateFailedIfNoValidValue() {
        Transfer transfer = getTransfer();
        doReturn(Optional.of(transfer)).when(transferRepository).findById(transfer.getId());

        assertThrows(IllegalArgumentException.class, () -> transferService.update(transfer.getId(), any()));
        verifyNoInteractions(transferReadMapper);
    }

    @Test
    void deleteSuccess() {
        Transfer transfer = getTransfer();
        doReturn(Optional.of(transfer)).when(transferRepository).findById(transfer.getId());
        doNothing().when(transferRepository).delete(transfer);

        boolean actualResult = transferService.delete(transfer.getId());

        assertThat(actualResult).isTrue();
    }

    @Test
    void deleteFailedIfTransferNotFound() {
        doThrow(EntityNotFoundException.class).when(transferRepository).findById(any());

        assertThrows(EntityNotFoundException.class, () -> transferService.delete(any()));
    }

    private static User getUser() {
        return User.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .passportNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB111111")
                .build();
    }

    private static Transfer getTransfer() {
        User user = getUser();
        return Transfer.builder()
                .id(1L)
                .user(user)
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .transferDate(LocalDate.now())
                .sum(BigDecimal.valueOf(50.00))
                .build();
    }

    private static UserReadDto getUserReadDto() {
        return UserReadDto.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .passportNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB111111")
                .build();
    }

    private static TransferReadDto getTransferReadDto() {
        UserReadDto userReadDto = getUserReadDto();
        return TransferReadDto.builder()
                .id(1L)
                .userReadDto(userReadDto)
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .transferDate(LocalDate.now())
                .sum(BigDecimal.valueOf(50.00))
                .build();
    }

    private static TransferCreateEditDto getTransferCreateEditDto() {
        User user = getUser();
        return TransferCreateEditDto.builder()
                .userId(user.getId())
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .sum(BigDecimal.valueOf(50.00))
                .build();
    }

    private static Page<Transfer> getPageTransfer() {
        List<Transfer> transferList = new ArrayList<>();
        User user = getUser();
        Transfer transfer1 = Transfer.builder()
                .id(1L)
                .user(user)
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .transferDate(LocalDate.now())
                .sum(BigDecimal.valueOf(50.00))
                .build();
        Transfer transfer2 = Transfer.builder()
                .id(2L)
                .user(user)
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .transferDate(LocalDate.now())
                .sum(BigDecimal.valueOf(50.00))
                .build();
        transferList.add(transfer1);
        transferList.add(transfer2);
        return new PageImpl<>(transferList);
    }

    private static List<Card> getListCard() {
        List<Card> cardList = new ArrayList<>();
        User user = getUser();
        Card card1 = Card.builder()
                .id(1L)
                .number(Base64Codec.encodeCardNumber("1234123412341234"))
                .user(user)
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
        Card card2 = Card.builder()
                .id(2L)
                .number(Base64Codec.encodeCardNumber("4321432143214321"))
                .user(user)
                .expirationDate(LocalDate.of(2035, 12, 12))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
        cardList.add(card1);
        cardList.add(card2);
        return cardList;
    }

    private static Card getCard() {
        User user = getUser();
        return Card.builder()
                .id(1L)
                .number(Base64Codec.encodeCardNumber("1234123412341234"))
                .user(user)
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
    }

    private static Predicate getPredicate(TransferFilter transferFilter) {
        return QPredicate.builder()
                .add(transferFilter.getTransferDate(), transfer.transferDate::after)
                .add(transferFilter.getCardFrom(), transfer.cardFrom::contains)
                .add(transferFilter.getCardTo(), transfer.cardTo::contains)
                .buildAnd();
    }

}