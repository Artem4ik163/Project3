// src/test/java/com/example/bank/BankServiceIntegrationTest.java
package com.example.bank;

import com.example.bank.service.BankService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class BankServiceIntegrationTest {

    @Autowired
    private BankService bankService;

    @Test
    void transferMoney_shouldTransferBetweenUsers() {
        // Given
        bankService.putMoney("alice", new BigDecimal("100"));
        bankService.putMoney("bob", new BigDecimal("50"));

        // When
        bankService.transferMoney("alice", "bob", new BigDecimal("30"));

        // Then
        assertThat(bankService.getBalance("alice")).isEqualByComparingTo("70");
        assertThat(bankService.getBalance("bob")).isEqualByComparingTo("80");
    }
}
