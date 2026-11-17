package com.shreya.riskengine.dto;

import com.shreya.riskengine.entity.RiskLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;


public record TransactionResponse(
        Long id,
        BigDecimal amount,
        String currency,
        String channel,
        boolean international,
        int riskScore,
        RiskLevel riskLevel,
        boolean flagged,
        LocalDateTime createdAt
) {}
