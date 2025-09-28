package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateEditDto;
import com.example.bankcards.dto.CardReadDto;
import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enumpack.Role;
import com.example.bankcards.enumpack.Status;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.mapper.CardCreateEditMapper;
import com.example.bankcards.mapper.CardReadMapper;
import com.example.bankcards.predicate.QPredicate;
import com.example.bankcards.repository.CardRepository;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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

import static com.example.bankcards.entity.QCard.card;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardReadMapper cardReadMapper;

    @Mock
    private CardCreateEditMapper cardCreateEditMapper;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void init() {
        cardService = new CardService(cardRepository, cardReadMapper, cardCreateEditMapper);
    }

    @Test
    void findAll() {
        CardFilter filter = CardFilter.builder().build();
        PageRequest pageable = PageRequest.of(0, 20);
        Card card = getCard();
        CardReadDto cardReadDto = getCardReadDto();
        Page<Card> pageCard = getPageCard();
        Predicate predicate = getPredicate(filter);
        doReturn(pageCard).when(cardRepository).findAll(predicate, pageable);
        doReturn(cardReadDto).when(cardReadMapper).map(card);

        Page<CardReadDto> actualResult = cardService.findAll(filter, pageable);

        assertThat(actualResult).hasSize(2);
        verifyNoInteractions(cardCreateEditMapper);
    }

    @Test
    void findAllByUserIdIfUserIdNoTExist() {
        CardFilter filter = CardFilter.builder().build();
        PageRequest pageable = PageRequest.of(0, 20);
        Predicate predicate = getPredicate(filter);
        User user = getUser();
        doReturn(Page.empty()).when(cardRepository).findAllByUserId(user.getId(), pageable, predicate);

        Page<CardReadDto> actualResult = cardService.findAllByUserId(user.getId(), pageable, filter);

        assertThat(actualResult).isEmpty();
        verifyNoInteractions(cardCreateEditMapper);
    }

    @Test
    void findByIdSuccess() {
        Card card = getCard();
        CardReadDto cardReadDto = getCardReadDto();
        Optional<Card> optionalCard = Optional.of(card);
        Optional<CardReadDto> optionalCardReadDto = Optional.of(cardReadDto);
        doReturn(optionalCard).when(cardRepository).findById(card.getId());
        doReturn(optionalCardReadDto.get()).when(cardReadMapper).map(optionalCard.get());

        CardReadDto actualResult = cardService.findById(card.getId());

        verifyNoInteractions(cardCreateEditMapper);
        assertThat(actualResult).isEqualTo(cardReadDto);
        verify(cardRepository).findById(card.getId());
    }

    @Test
    void findByIdFailedCardNotExist() {
        doReturn(Optional.empty()).when(cardRepository).findById(any());

        verifyNoInteractions(cardCreateEditMapper, cardReadMapper);
        assertThrows(EntityNotFoundException.class, () -> cardService.findById(any()));
    }

    @Test
    void getBalanceSuccess() {
        Card card = getCard();
        Optional<Card> optionalCard = Optional.of(card);
        doReturn(optionalCard).when(cardRepository).findById(card.getId());

        BigDecimal actualResult = cardService.getBalance(card.getId());

        verifyNoInteractions(cardCreateEditMapper);
        assertThat(actualResult).isEqualTo(card.getBalance());
        verify(cardRepository).findById(card.getId());
    }

    @Test
    void getBalanceFailedIfCardNotExist() {
        doReturn(Optional.empty()).when(cardRepository).findById(any());

        verifyNoInteractions(cardCreateEditMapper, cardReadMapper);
        assertThrows(EntityNotFoundException.class, () -> cardService.getBalance(any()));
    }

    @Test
    void createSuccess() {
        Card card = getCard();
        CardCreateEditDto cardCreateEditDto = getCardCreateEditDto();
        CardReadDto cardReadDto = getCardReadDto();
        doReturn(card).when(cardCreateEditMapper).map(cardCreateEditDto);
        doReturn(card).when(cardRepository).save(card);
        doReturn(cardReadDto).when(cardReadMapper).map(card);

        CardReadDto actualResult = cardService.create(cardCreateEditDto);

        assertThat(actualResult.getId()).isEqualTo(card.getId());
        verify(cardRepository).save(card);
    }

    @Test
    void createFailedIfNoValidValue() {
        CardCreateEditDto cardCreateEditDto = CardCreateEditDto.builder().build();

        assertThrows(IllegalArgumentException.class, () -> cardService.create(cardCreateEditDto));
        verifyNoInteractions(cardReadMapper, cardRepository);
    }

    @Test
    void updateSuccess() {
        Card card = getCard();
        CardReadDto cardReadDto = getCardReadDto();
        CardCreateEditDto cardCreateEditDto = getCardCreateEditDto();
        doReturn(Optional.of(card)).when(cardRepository).findById(card.getId());
        doReturn(card).when(cardCreateEditMapper).map(cardCreateEditDto, card);
        doReturn(card).when(cardRepository).saveAndFlush(card);
        doReturn(cardReadDto).when(cardReadMapper).map(card);

        CardReadDto actualResult = cardService.update(card.getId(), cardCreateEditDto);

        assertThat(actualResult).isEqualTo(cardReadDto);
        verify(cardRepository).saveAndFlush(card);
        verify(cardRepository).findById(card.getId());
    }

    @Test
    void updateFailedIfCardNotFound() {
        doThrow(EntityNotFoundException.class).when(cardRepository).findById(any());

        assertThrows(EntityNotFoundException.class, () -> cardService.update(any(), getCardCreateEditDto()));
        verifyNoInteractions(cardReadMapper, cardCreateEditMapper);
    }

    @Test
    void updateFailedIfNoValidValue() {
        Card card = getCard();
        doReturn(Optional.of(card)).when(cardRepository).findById(card.getId());

        assertThrows(IllegalArgumentException.class, () -> cardService.update(card.getId(), any()));
        verifyNoInteractions(cardReadMapper);
    }

    @Test
    void blockingCardSuccess() {
        Card card = getCard();
        CardReadDto cardReadDto = CardReadDto.builder()
                .status(Status.BLOCKED)
                .build();
        doReturn(Optional.of(card)).when(cardRepository).findById(card.getId());
        doReturn(card).when(cardRepository).saveAndFlush(card);
        doReturn(cardReadDto).when(cardReadMapper).map(card);

        CardReadDto actualResult = cardService.blockingCard(card.getId());

        assertThat(actualResult.getStatus()).isEqualTo(Status.BLOCKED);
        verify(cardRepository).findById(card.getId());
        verify(cardRepository).saveAndFlush(card);
    }

    @Test
    void blockingCardFailedIfCardNotFound() {
        doReturn(Optional.empty()).when(cardRepository).findById(any());

        assertThrows(EntityNotFoundException.class, () -> cardService.blockingCard(any()));
        verifyNoInteractions(cardReadMapper);
    }

    @Test
    void blockingCardFailedIfStatusCardIsBlocked() {
        Card card = getCard();
        card.setStatus(Status.BLOCKED);
        doReturn(Optional.of(card)).when(cardRepository).findById(card.getId());

        assertThrows(IllegalArgumentException.class, () -> cardService.blockingCard(card.getId()));
        verifyNoInteractions(cardReadMapper);
        verify(cardRepository).findById(card.getId());
    }

    @Test
    void blockingCardFailedIfStatusCardIsExpired() {
        Card card = getCard();
        card.setStatus(Status.EXPIRED);
        doReturn(Optional.of(card)).when(cardRepository).findById(card.getId());

        assertThrows(IllegalArgumentException.class, () -> cardService.blockingCard(card.getId()));
        verifyNoInteractions(cardReadMapper);
        verify(cardRepository).findById(card.getId());
    }

    @Test
    void deleteSuccess() {
        Card card = getCard();
        doReturn(Optional.of(card)).when(cardRepository).findById(card.getId());
        doNothing().when(cardRepository).delete(card);

        boolean actualResult = cardService.delete(card.getId());

        assertThat(actualResult).isTrue();
    }

    @Test
    void deleteFailedIfCardNotFound() {
        doThrow(EntityNotFoundException.class).when(cardRepository).findById(any());

        assertThrows(EntityNotFoundException.class, () -> cardService.delete(any()));
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

    private static Card getCard() {
        User user = getUser();
        return Card.builder()
                .id(1L)
                .number("1234123412341234")
                .user(user)
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
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

    private static CardReadDto getCardReadDto() {
        UserReadDto userReadDto = getUserReadDto();
        return CardReadDto.builder()
                .id(1L)
                .number("1234132412341234")
                .userReadDto(userReadDto)
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(0.00))
                .build();
    }

    private static CardCreateEditDto getCardCreateEditDto() {
        User user = getUser();
        return CardCreateEditDto.builder()
                .number("111111")
                .userId(user.getId())
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(0.00))
                .build();
    }

    private static Page<Card> getPageCard() {
        List<Card> cardList = new ArrayList<>();
        User user = getUser();
        Card card1 = Card.builder()
                .id(1L)
                .number("1234123412341234")
                .user(user)
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
        Card card2 = Card.builder()
                .id(2L)
                .number("4321432143214321")
                .user(user)
                .expirationDate(LocalDate.of(2035, 12, 12))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
        cardList.add(card1);
        cardList.add(card2);
        return new PageImpl<>(cardList);
    }

    private static Predicate getPredicate(CardFilter cardFilter) {
        return  QPredicate.builder()
                .add(cardFilter.getNumber(), card.number::contains)
                .add(cardFilter.getExpirationDate(), card.expirationDate::before)
                .buildAnd();
    }
}
