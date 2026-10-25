package com.shreya.riskengine.service;

import com.shreya.riskengine.dto.TransactionRequest;
import com.shreya.riskengine.entity.RiskLevel;
import com.shreya.riskengine.entity.Transaction;
import com.shreya.riskengine.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionService service;

    @Test
    void allRiskRulesShouldProduceHighRiskAndFlagTransaction() {
        TransactionRequest request = new TransactionRequest(
            new BigDecimal("85000"), "INR", "ONLINE", true);

        when(repository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.create(request);

        assertEquals(90, result.getRiskScore());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.isFlagged());
        verify(repository).save(any(Transaction.class));
    }

    @Test
    void branchTransactionWithLowAmountShouldBeLowRisk() {
        TransactionRequest request = new TransactionRequest(
            new BigDecimal("1000"), "INR", "BRANCH", false);

        when(repository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.create(request);

        assertEquals(0, result.getRiskScore());
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertFalse(result.isFlagged());
    }

    @Test
    void internationalOnlyTransactionShouldBeMediumRisk() {
        TransactionRequest request = new TransactionRequest(
            new BigDecimal("1000"), "INR", "BRANCH", true);

        when(repository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.create(request);

        assertEquals(30, result.getRiskScore());
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
        assertFalse(result.isFlagged());
    }

    @Test
    void maximumCombinationOfCurrentRiskRulesShouldScoreNinety() {
        TransactionRequest request = new TransactionRequest(
            new BigDecimal("100000"), "INR", "ONLINE", true);

        when(repository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.create(request);

        assertEquals(90, result.getRiskScore());
        assertTrue(result.isFlagged());
    }


    @Test
    void amountAtThresholdWithOnlineChannelShouldBeHighRisk() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("50000"), "INR", "ONLINE", false
        );

        when(repository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = service.create(request);

        assertEquals(60, result.getRiskScore());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.isFlagged());
    }

    @Test
    void getByIdForMissingTransactionShouldThrowNotFound() {
        when(repository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getById(999L)
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void invalidPageSizeShouldThrowBadRequest() {
        assertThrows(
                ResponseStatusException.class,
                () -> service.getAll(null, 0, 0)
        );

        verifyNoInteractions(repository);
    }


}
