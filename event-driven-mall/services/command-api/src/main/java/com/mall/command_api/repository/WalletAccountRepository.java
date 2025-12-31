package com.mall.command_api.repository;

import com.mall.command_api.entity.WalletAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, String> {
}
