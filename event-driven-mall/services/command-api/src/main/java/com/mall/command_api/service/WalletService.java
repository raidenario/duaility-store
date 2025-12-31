package com.mall.command_api.service;

import com.mall.command_api.entity.WalletAccount;
import com.mall.command_api.repository.WalletAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletAccountRepository walletAccountRepository;
    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal("3000.00");

    public WalletAccount getOrCreate(String userId) {
        return walletAccountRepository.findById(userId).orElseGet(() -> {
            WalletAccount acc = new WalletAccount();
            acc.setUserId(userId);
            acc.setBalance(DEFAULT_BALANCE);
            return walletAccountRepository.save(acc);
        });
    }
}
