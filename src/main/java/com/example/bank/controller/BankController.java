package com.example.bank.controller;

import com.example.bank.model.ApiResponse;
import com.example.bank.model.Transaction;
import com.example.bank.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    @Autowired
    private BankService bankService;

    @GetMapping("/getBalance")
    public ResponseEntity<ApiResponse> getBalance(@RequestParam String userId) {
        try {
            BigDecimal balance = bankService.getBalance(userId);
            return ResponseEntity.ok(new ApiResponse(1, balance.toString()));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(-1, "Ошибка при выполнении операции"));
        }
    }

    @PostMapping("/putMoney")
    public ResponseEntity<ApiResponse> putMoney(
            @RequestParam String userId,
            @RequestParam BigDecimal amount) {
        try {
            bankService.putMoney(userId, amount);
            return ResponseEntity.ok(new ApiResponse(1, "Успех"));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(0, "Ошибка при выполнении операции"));
        }
    }

    @PostMapping("/takeMoney")
    public ResponseEntity<ApiResponse> takeMoney(
            @RequestParam String userId,
            @RequestParam BigDecimal amount) {
        try {
            bankService.takeMoney(userId, amount);
            return ResponseEntity.ok(new ApiResponse(1, "Успех"));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(new ApiResponse(0, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(0, "Ошибка при выполнении операции"));
        }
    }

    @PostMapping("/transferMoney")
    public ResponseEntity<ApiResponse> transferMoney(
            @RequestParam String currentUserId,
            @RequestParam String targetUserId,
            @RequestParam BigDecimal amount) {
        try {
            bankService.transferMoney(currentUserId, targetUserId, amount);
            return ResponseEntity.ok(new ApiResponse(1, "Успех"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new ApiResponse(0, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(new ApiResponse(0, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(0, "Ошибка при выполнении операции"));
        }
    }

    @GetMapping("/getOperationList")
    public ResponseEntity<?> getOperationList(
            @RequestParam String userId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {

        LocalDateTime dtFrom = null;
        LocalDateTime dtTo = null;

        if (from != null && !from.trim().isEmpty()) {
            try {
                dtFrom = LocalDateTime.parse(from);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse(-1, "Неверный формат даты 'from'. Используйте: yyyy-MM-ddTHH:mm:ss"));
            }
        }

        if (to != null && !to.trim().isEmpty()) {
            try {
                dtTo = LocalDateTime.parse(to);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse(-1, "Неверный формат даты 'to'. Используйте: yyyy-MM-ddTHH:mm:ss"));
            }
        }

        try {
            List<Transaction> transactions = bankService.getOperationList(userId, dtFrom, dtTo);
            var result = transactions.stream().map(tx -> {
                String typeStr;
                switch (tx.getOperationType()) {
                    case 1: typeStr = "пополнение счета"; break;
                    case 2: typeStr = "снятие со счета"; break;
                    case 3: typeStr = "перевод другому клиенту"; break;
                    case 4: typeStr = "перевод от другого клиента"; break;
                    default: typeStr = "неизвестная операция";
                }
                return Map.of(
                        "date", tx.getCreatedAt(),
                        "type", typeStr,
                        "amount", tx.getAmount()
                );
            }).toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(-1, "Ошибка при получении операций"));
        }
    }
}
