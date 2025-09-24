package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateEditDto;
import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.enumpack.Role;
import com.example.bankcards.filter.UserFilter;
import com.example.bankcards.mapper.UserCreateEditMapper;
import com.example.bankcards.mapper.UserReadMapper;
import com.example.bankcards.predicate.QPredicate;
import com.example.bankcards.repository.UserRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.QUser.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserReadMapper userReadMapper;

    @Mock
    private UserCreateEditMapper userCreateEditMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void findAll() {
        UserFilter filter = UserFilter.builder()
                .lastname("ov")
                .birthDate(LocalDate.of(2020, 5, 15))
                .build();
        PageRequest pageable = PageRequest.of(0, 20);
        User user = getUser();
        UserReadDto userReadDto = getUserReadDto();
        Page<User> pageUser = getPageUser();
        Predicate predicate = getPredicate(filter);
        doReturn(pageUser).when(userRepository).findAll(predicate, pageable);
        doReturn(userReadDto).when(userReadMapper).map(user);

        Page<UserReadDto> actualResult = userService.findAll(filter, pageable);

        assertThat(actualResult).hasSize(2);
        verifyNoInteractions(userCreateEditMapper);
    }

    @Test
    void findByIdSuccess() {
        User user = getUser();
        UserReadDto userReadDto = getUserReadDto();
        Optional<User> optionalUser = Optional.ofNullable(user);
        Optional<UserReadDto> optionalUserReadDto = Optional.ofNullable(userReadDto);
        doReturn(optionalUser).when(userRepository).findById(user.getId());
        doReturn(optionalUserReadDto.get()).when(userReadMapper).map(optionalUser.get());

        UserReadDto actualResult = userService.findById(user.getId());

        verifyNoInteractions(userCreateEditMapper);
        assertThat(actualResult).isEqualTo(userReadDto);
        verify(userRepository).findById(user.getId());
    }

    @Test
    void findByIdFailed() {
        doReturn(Optional.empty()).when(userRepository).findById(any());

        verifyNoInteractions(userCreateEditMapper, userReadMapper);
        assertThrows(EntityNotFoundException.class, () -> userService.findById(any()));
    }

    @Test
    void createSuccess() {
        User user = getUser();
        UserCreateEditDto userCreateEditDto = getUserCreateEditDto();
        UserReadDto userReadDto = getUserReadDto();
        doReturn(user).when(userCreateEditMapper).map(userCreateEditDto);
        doReturn(user).when(userRepository).save(user);
        doReturn(userReadDto).when(userReadMapper).map(user);

        UserReadDto actualResult = userService.create(userCreateEditDto);

        assertThat(actualResult.getId()).isEqualTo(user.getId());
        verify(userRepository).save(user);
    }

    @Test
    void createFailedIfNoValidValue() {
        UserCreateEditDto userCreateEditDto = UserCreateEditDto.builder().build();

        assertThrows(IllegalArgumentException.class, () -> userService.create(userCreateEditDto));
        verifyNoInteractions(userReadMapper, userRepository);
    }

    @Test
    void updateSuccess() {
        User user = getUser();
        UserReadDto userReadDto = getUserReadDto();
        UserCreateEditDto userCreateEditDto = getUserCreateEditDto();
        doReturn(Optional.ofNullable(user)).when(userRepository).findById(user.getId());
        doReturn(user).when(userCreateEditMapper).map(userCreateEditDto, user);
        doReturn(user).when(userRepository).saveAndFlush(user);
        doReturn(userReadDto).when(userReadMapper).map(user);

        UserReadDto actualResult = userService.update(user.getId(), userCreateEditDto);

        assertThat(actualResult).isEqualTo(userReadDto);
        verify(userRepository).saveAndFlush(user);
        verify(userRepository).findById(user.getId());
    }

    @Test
    void updateFailedIfEntityNotFound() {
        doThrow(EntityNotFoundException.class).when(userRepository).findById(any());

        assertThrows(EntityNotFoundException.class, () -> userService.update(any(), getUserCreateEditDto()));
        verifyNoInteractions(userReadMapper, userCreateEditMapper);
    }

    @Test
    void updateFailedIfNoValidValue() {
        User user = getUser();
        doReturn(Optional.ofNullable(user)).when(userRepository).findById(user.getId());

        assertThrows(IllegalArgumentException.class, () -> userService.update(user.getId(), any()));
        verifyNoInteractions(userReadMapper);
    }

    @Test
    void deleteSuccess() {
        User user = getUser();
        doReturn(Optional.ofNullable(user)).when(userRepository).findById(user.getId());
        doNothing().when(userRepository).delete(user);

        boolean actualResult = userService.delete(user.getId());

        assertThat(actualResult).isTrue();
    }

    @Test
    void deleteFailedIfEntityNotFound() {
        doThrow(EntityNotFoundException.class).when(userRepository).findById(any());

        assertThrows(EntityNotFoundException.class, () -> userService.delete(any()));
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

    private static UserCreateEditDto getUserCreateEditDto() {
        return UserCreateEditDto.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .passportNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB111111")
                .build();
    }


    private static Page<UserReadDto> getPageUserReadDto() {
        List<UserReadDto> userReadDtoList = new ArrayList<>();
        UserReadDto ivan = UserReadDto.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .passportNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB111111")
                .build();
        UserReadDto petr = UserReadDto.builder()
                .id(2L)
                .firstname("Petr")
                .lastname("Petrov")
                .passportNumber("22-22-222")
                .email("petr@gmail.com")
                .role(Role.ADMIN)
                .birthDate(LocalDate.of(2005, 12, 22))
                .passportNumber("HB22222")
                .build();
        userReadDtoList.add(ivan);
        userReadDtoList.add(petr);
        return new PageImpl<>(userReadDtoList);
    }

    private static Page<User> getPageUser() {
        List<User> userList = new ArrayList<>();
        User ivan = User.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .passportNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB111111")
                .build();
        User petr = User.builder()
                .id(2L)
                .firstname("Petr")
                .lastname("Petrov")
                .passportNumber("22-22-222")
                .email("petr@gmail.com")
                .role(Role.ADMIN)
                .birthDate(LocalDate.of(2005, 12, 22))
                .passportNumber("HB22222")
                .build();
        userList.add(ivan);
        userList.add(petr);
        return new PageImpl<>(userList);
    }

    private static Predicate getPredicate(UserFilter userFilter) {
        return QPredicate.builder()
                .add(userFilter.getFirstname(), user.firstname::containsIgnoreCase)
                .add(userFilter.getLastname(), user.lastname::containsIgnoreCase)
                .add(userFilter.getPhoneNumber(), user.phoneNumber::containsIgnoreCase)
                .add(userFilter.getEmail(), user.email::containsIgnoreCase)
                .add(userFilter.getBirthDate(), user.birthDate::before)
                .add(userFilter.getPassportNumber(), user.passportNumber::containsIgnoreCase)
                .buildAnd();
    }
}
