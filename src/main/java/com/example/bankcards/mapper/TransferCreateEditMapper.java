package com.example.bankcards.mapper;

import com.example.bankcards.dto.TransferCreateEditDto;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.Base64Codec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransferCreateEditMapper implements Mapper<TransferCreateEditDto, Transfer> {

    private final UserRepository userRepository;

    @Override
    public Transfer map(TransferCreateEditDto transferCreateEditDto) {
        Transfer transfer = new Transfer();
        copy(transferCreateEditDto, transfer);
        return transfer;
    }

    @Override
    public Transfer map(TransferCreateEditDto transferCreateEditDto, Transfer transfer) {
        copy(transferCreateEditDto, transfer);
        return transfer;
    }

    private void copy(TransferCreateEditDto transferCreateEditDto, Transfer transfer) {
        transfer.setUser(getUser(transferCreateEditDto.getUserId()));
        transfer.setCardFrom(Base64Codec.encodeCardNumber(transferCreateEditDto.getCardFrom()));
        transfer.setCardTo(Base64Codec.encodeCardNumber(transferCreateEditDto.getCardTo()));
        transfer.setTransferDate(transferCreateEditDto.getTransferDate());
        transfer.setSum(transferCreateEditDto.getSum());
    }

    private User getUser(Long userId) {
        return Optional.ofNullable(userId)
                .flatMap(userRepository::findById)
                .orElseThrow();
    }
}
