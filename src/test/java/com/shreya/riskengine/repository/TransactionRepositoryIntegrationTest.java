package com.shreya.riskengine.repository;

import com.shreya.riskengine.entity.RiskLevel;
import com.shreya.riskengine.entity.Transaction;
import com.shreya.riskengine.repository.specification.TransactionSpecification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryIntegrationTest {

    @Autowired
    private TransactionRepository repository;

    @BeforeEach
    void setUp() {

        repository.deleteAll();

        Transaction t1 = new Transaction(
                new BigDecimal("85000"),
                "INR",
                "ONLINE",
                true
        );
        t1.setRiskScore(90);
        t1.setRiskLevel(RiskLevel.HIGH);
        t1.setFlagged(true);

        Transaction t2 = new Transaction(
                new BigDecimal("10000"),
                "INR",
                "BRANCH",
                false
        );
        t2.setRiskScore(0);
        t2.setRiskLevel(RiskLevel.LOW);
        t2.setFlagged(false);

        Transaction t3 = new Transaction(
                new BigDecimal("60000"),
                "USD",
                "ONLINE",
                false
        );
        t3.setRiskScore(60);
        t3.setRiskLevel(RiskLevel.HIGH);
        t3.setFlagged(true);

        repository.saveAll(java.util.List.of(t1, t2, t3));
        repository.flush();
    }

    @Test
    void filterByRiskLevelShouldReturnOnlyHighRiskTransactions() {

        Specification<Transaction> specification =
                TransactionSpecification.filterTransactions(
                        RiskLevel.HIGH,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        Page<Transaction> result = repository.findAll(
                specification,
                PageRequest.of(0, 10)
        );

        assertEquals(2, result.getTotalElements());

        assertTrue(result.getContent().stream()
                .allMatch(t -> t.getRiskLevel() == RiskLevel.HIGH));
    }

    @Test
    void multipleFiltersShouldReturnOnlyMatchingTransactions() {

        Specification<Transaction> specification =
                TransactionSpecification.filterTransactions(
                        RiskLevel.HIGH,
                        "ONLINE",
                        "INR",
                        new BigDecimal("50000"),
                        new BigDecimal("100000"),
                        true,
                        null,
                        null
                );

        Page<Transaction> result = repository.findAll(
                specification,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());

        Transaction transaction = result.getContent().get(0);

        assertEquals(new BigDecimal("85000"), transaction.getAmount());
        assertEquals("INR", transaction.getCurrency());
        assertEquals("ONLINE", transaction.getChannel());
        assertEquals(RiskLevel.HIGH, transaction.getRiskLevel());
        assertTrue(transaction.isInternational());
    }

    @Test
    void filterByChannelShouldReturnOnlyOnlineTransactions() {

        Specification<Transaction> specification =
                TransactionSpecification.filterTransactions(
                        null,
                        "ONLINE",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        Page<Transaction> result = repository.findAll(
                specification,
                PageRequest.of(0, 10)
        );

        assertEquals(2, result.getTotalElements());

        assertTrue(result.getContent().stream()
                .allMatch(t -> "ONLINE".equals(t.getChannel())));
    }
}