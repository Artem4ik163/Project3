package com.example.bank.service;

import com.example.bank.model.Account;
import com.example.bank.model.Transaction;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BankService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public BigDecimal getBalance(String userId) {
        return accountRepository.findByUserId(userId)
                .map(Account::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public void putMoney(String userId, BigDecimal amount) {
        validateAmount(amount);
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        Account account = accountRepository.findByUserId(userId)
                .orElseGet(() -> new Account(userId));
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setOperationType(1);
        tx.setAmount(amount);
        transactionRepository.save(tx);
    }

    @Transactional
    public void takeMoney(String userId, BigDecimal amount) {
        validateAmount(amount);
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Счёт не найден"));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Недостаточно средств");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setOperationType(2);
        tx.setAmount(amount);
        transactionRepository.save(tx);
    }

    @Transactional
    public void transferMoney(String fromUserId, String toUserId, BigDecimal amount) {
        if (fromUserId == null || toUserId == null || fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Некорректные ID пользователей");
        }
        validateAmount(amount);
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        // Снимаем у отправителя
        Account fromAccount = accountRepository.findByUserId(fromUserId)
                .orElseThrow(() -> new IllegalStateException("Счёт отправителя не найден"));
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Недостаточно средств");
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        // Пополняем получателя
        Account toAccount = accountRepository.findByUserId(toUserId)
                .orElseGet(() -> {
                    Account newAccount = new Account(toUserId);
                    accountRepository.save(newAccount);
                    return newAccount;
                });
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);

        // Логируем обе операции
        Transaction txFrom = new Transaction();
        txFrom.setUserId(fromUserId);
        txFrom.setTargetUserId(toUserId);
        txFrom.setOperationType(3);
        txFrom.setAmount(amount);
        transactionRepository.save(txFrom);

        Transaction txTo = new Transaction();
        txTo.setUserId(toUserId);
        txTo.setTargetUserId(fromUserId);
        txTo.setOperationType(4);
        txTo.setAmount(amount);
        transactionRepository.save(txTo);
    }

    public List<Transaction> getOperationList(String userId, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findTransactionsByUserIdAndDateRange(userId, from, to);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
    }
}
