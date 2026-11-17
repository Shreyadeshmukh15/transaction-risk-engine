package com.shreya.riskengine.service;

import com.shreya.riskengine.dto.TransactionRequest;
import com.shreya.riskengine.dto.TransactionResponse;
import com.shreya.riskengine.entity.RiskLevel;
import com.shreya.riskengine.entity.Transaction;
import com.shreya.riskengine.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.shreya.riskengine.repository.specification.TransactionSpecification;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000");

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public TransactionResponse create(TransactionRequest request) {

        String currency = request.currency().trim().toUpperCase();
        String channel = request.channel().trim().toUpperCase();

        Transaction transaction = new Transaction(
                request.amount(),
                currency,
                channel,
                request.international()
        );

        int score = 0;

        if (request.amount().compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            score += 40;
        }
        if ("ONLINE".equals(request.channel())) {
            score += 20;
        }
        if (Boolean.TRUE.equals(request.international())) {
            score += 30;
        }

        score = Math.min(score, 100);

        RiskLevel level;
        if (score >= 60) {
            level = RiskLevel.HIGH;
        } else if (score >= 30) {
            level = RiskLevel.MEDIUM;
        } else {
            level = RiskLevel.LOW;
        }

        transaction.setRiskScore(score);
        transaction.setRiskLevel(level);
        transaction.setFlagged(level == RiskLevel.HIGH);

        return mapToResponse(repository.save(transaction));
    }

    public TransactionResponse getById(Long id) {
        return mapToResponse(repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transaction not found: " + id)));
    }


    public Page<TransactionResponse> getAll(
            RiskLevel riskLevel,
            String channel,
            String currency,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Boolean international,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size) {

        if (page < 0 || size <= 0 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum amount must be greater than zero"
            );
        }

        if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum amount must be greater than zero"
            );
        }

        if (minAmount != null && maxAmount != null
                && minAmount.compareTo(maxAmount) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum amount cannot exceed maximum amount"
            );
        }

        if (fromDate != null && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "From date cannot be after to date"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Specification<Transaction> specification =
                TransactionSpecification.filterTransactions(
                        riskLevel,
                        channel,
                        currency,
                        minAmount,
                        maxAmount,
                        international,
                        fromDate,
                        toDate
                );

        return repository.findAll(specification, pageable)
                .map(this::mapToResponse);
    }



    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getChannel(),
                transaction.isInternational(),
                transaction.getRiskScore(),
                transaction.getRiskLevel(),
                transaction.isFlagged(),
                transaction.getCreatedAt()
        );
    }


}
