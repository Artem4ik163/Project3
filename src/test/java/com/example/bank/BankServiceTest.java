package com.example.bank;

import com.example.bank.model.Account;
import com.example.bank.repository.AccountRepository;
import com.example.bank.service.BankService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class BankServiceTest {

    @Autowired
    private BankService bankService;

    @MockBean
    private AccountRepository accountRepository;

    @Test
    void putMoney_shouldIncreaseBalance() {
        // Given
        String userId = "testUser";
        Account account = new Account(userId);
        account.setBalance(BigDecimal.ZERO);
        when(accountRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(account));

        // When
        bankService.putMoney(userId, new BigDecimal("50.00"));

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo("50.00");
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}
