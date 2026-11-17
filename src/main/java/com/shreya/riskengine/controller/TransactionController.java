package com.shreya.riskengine.controller;

import com.shreya.riskengine.dto.TransactionRequest;
import com.shreya.riskengine.dto.TransactionResponse;
import com.shreya.riskengine.entity.RiskLevel;
import com.shreya.riskengine.entity.Transaction;
import com.shreya.riskengine.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public Page<TransactionResponse> getAll(
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getAll(riskLevel, page, size);
    }
}
