package com.example.bankcards.mapper;


import com.example.bankcards.dto.TransferReadDto;
import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.entity.Transfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransferReadMapper implements Mapper<Transfer, TransferReadDto> {

    private final UserReadMapper userReadMapper;

    @Override
    public TransferReadDto map(Transfer transfer) {

        UserReadDto userReadDto = Optional.ofNullable(transfer.getUser())
                .map(userReadMapper::map)
                .orElseThrow();

        return new TransferReadDto(
                transfer.getId(),
                userReadDto,
                transfer.getCardFrom(),
                transfer.getCardTo(),
                transfer.getTransferDate(),
                transfer.getSum()
        );
    }
}
