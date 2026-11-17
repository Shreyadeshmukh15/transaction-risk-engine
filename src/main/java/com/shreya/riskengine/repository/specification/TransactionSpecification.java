
package com.shreya.riskengine.repository.specification;

import com.shreya.riskengine.entity.Transaction;
import com.shreya.riskengine.entity.RiskLevel;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> filterTransactions(
            RiskLevel riskLevel,
            String channel,
            String currency,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Boolean international,
            LocalDateTime fromDate,
            LocalDateTime toDate) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (riskLevel != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("riskLevel"), riskLevel));
            }

            if (channel != null && !channel.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("channel")),
                                channel.trim().toUpperCase()));
            }

            if (currency != null && !currency.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("currency")),
                                currency.trim().toUpperCase()));
            }

            if (minAmount != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("amount"), minAmount));
            }

            if (maxAmount != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("amount"), maxAmount));
            }

            if (international != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("international"), international));
            }

            if (fromDate != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("createdAt"), toDate));
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0]));
        };
    }
}
