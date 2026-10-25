package com.shreya.riskengine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(nullable = false)
    private boolean international;

    @Column(nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RiskLevel riskLevel;

    @Column(nullable = false)
    private boolean flagged;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Transaction() {
        // Required by JPA
    }

    public Transaction(BigDecimal amount, String currency,
                       String channel, boolean international) {
        this.amount = amount;
        this.currency = currency;
        this.channel = channel;
        this.international = international;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getChannel() { return channel; }
    public boolean isInternational() { return international; }
    public int getRiskScore() { return riskScore; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public boolean isFlagged() { return flagged; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }
}
