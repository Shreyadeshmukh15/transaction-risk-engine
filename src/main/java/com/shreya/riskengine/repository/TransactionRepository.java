package com.shreya.riskengine.repository;

import com.shreya.riskengine.entity.RiskLevel;
import com.shreya.riskengine.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    Page<Transaction> findByRiskLevel(RiskLevel riskLevel, Pageable pageable);
}
