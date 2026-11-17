
package com.shreya.riskengine.service;

import com.shreya.riskengine.dto.TransactionRequest;
import com.shreya.riskengine.dto.TransactionResponse;
import com.shreya.riskengine.entity.RiskLevel;
import com.shreya.riskengine.entity.Transaction;
import com.shreya.riskengine.repository.TransactionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionService service;

    @Test
    void allRiskRulesShouldProduceHighRiskAndFlagTransaction() {

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("85000"), "INR", "ONLINE", true
        );

        when(repository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse result = service.create(request);

        assertEquals(90, result.riskScore());
        assertEquals(RiskLevel.HIGH, result.riskLevel());
        assertTrue(result.flagged());

        verify(repository).save(any(Transaction.class));
    }

    @Test
    void branchTransactionWithLowAmountShouldBeLowRisk() {

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("1000"), "INR", "BRANCH", false
        );

        when(repository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse result = service.create(request);

        assertEquals(0, result.riskScore());
        assertEquals(RiskLevel.LOW, result.riskLevel());
        assertFalse(result.flagged());
    }

    @Test
    void internationalOnlyTransactionShouldBeMediumRisk() {

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("1000"), "INR", "BRANCH", true
        );

        when(repository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse result = service.create(request);

        assertEquals(30, result.riskScore());
        assertEquals(RiskLevel.MEDIUM, result.riskLevel());
        assertFalse(result.flagged());
    }

    @Test
    void maximumCombinationOfCurrentRiskRulesShouldScoreNinety() {

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100000"), "INR", "ONLINE", true
        );

        when(repository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse result = service.create(request);

        assertEquals(90, result.riskScore());
        assertEquals(RiskLevel.HIGH, result.riskLevel());
        assertTrue(result.flagged());
    }

    @Test
    void amountAtThresholdWithOnlineChannelShouldBeHighRisk() {

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("50000"), "INR", "ONLINE", false
        );

        when(repository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse result = service.create(request);

        assertEquals(60, result.riskScore());
        assertEquals(RiskLevel.HIGH, result.riskLevel());
        assertTrue(result.flagged());
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
                () -> service.getAll(
                        null, null, null,
                        null, null, null,
                        null, null,
                        0, 0
                )
        );

        verifyNoInteractions(repository);
    }


    @Test
    void getAllWithFiltersShouldReturnPagedResults() {

        when(repository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        var result = service.getAll(
                RiskLevel.HIGH,
                "ONLINE",
                "INR",
                new BigDecimal("50000"),
                new BigDecimal("100000"),
                true,
                null,
                null,
                0,
                10
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(repository).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }


    @Test
    void minimumAmountGreaterThanMaximumShouldThrowBadRequest() {

        assertThrows(
                ResponseStatusException.class,
                () -> service.getAll(
                        null, null, null,
                        new BigDecimal("100000"),
                        new BigDecimal("50000"),
                        null, null, null,
                        0, 10
                )
        );

        verifyNoInteractions(repository);
    }


}
