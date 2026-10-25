package com.shreya.riskengine.service;

import com.shreya.riskengine.dto.TransactionRequest;
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

import java.math.BigDecimal;

@Service
public class TransactionService {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000");

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction create(TransactionRequest request) {
        Transaction transaction = new Transaction(
            request.amount(),
            request.currency(),
            request.channel(),
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

        return repository.save(transaction);
    }

    public Transaction getById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Transaction not found: " + id));
    }

    public Page<Transaction> getAll(RiskLevel riskLevel, int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(
            page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return riskLevel == null
            ? repository.findAll(pageable)
            : repository.findByRiskLevel(riskLevel, pageable);
    }
}
