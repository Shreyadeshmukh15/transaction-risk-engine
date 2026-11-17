
package com.shreya.riskengine.controller;

import com.shreya.riskengine.dto.TransactionRequest;
import com.shreya.riskengine.dto.TransactionResponse;
import com.shreya.riskengine.entity.RiskLevel;
import com.shreya.riskengine.service.TransactionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.data.domain.Page;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService service;

    @Test
    void createValidTransactionShouldReturn201() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                1L,
                new BigDecimal("85000"),
                "INR",
                "ONLINE",
                true,
                90,
                RiskLevel.HIGH,
                true,
                LocalDateTime.now()
        );

        when(service.create(any(TransactionRequest.class)))
                .thenReturn(transaction);

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("85000"),
                "INR",
                "ONLINE",
                true
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.riskScore").value(90))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.flagged").value(true));

        verify(service).create(any(TransactionRequest.class));
    }

    @Test
    void createInvalidTransactionShouldReturn400() throws Exception {

        String invalidRequest = """
                {
                    "amount": -100,
                    "currency": "INR",
                    "channel": "ONLINE",
                    "international": false
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any(TransactionRequest.class));
    }

    @Test
    void invalidRiskLevelParameterShouldReturn400() throws Exception {

        mockMvc.perform(get("/api/transactions")
                        .param("riskLevel", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter: riskLevel"));
    }

    @Test
    void unsupportedCurrencyShouldReturn400() throws Exception {

        String invalidRequest = """
                {
                    "amount": 1000,
                    "currency": "HVH",
                    "channel": "ONLINE",
                    "international": false
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());

        verify(service, never()).create(any(TransactionRequest.class));
    }


    @Test
    void getTransactionsWithMultipleFiltersShouldReturn200() throws Exception {

        when(service.getAll(
                any(), any(), any(), any(),
                any(), any(), any(), any(),
                anyInt(), anyInt()
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/transactions")
                        .param("riskLevel", "HIGH")
                        .param("channel", "ONLINE")
                        .param("currency", "INR")
                        .param("minAmount", "50000")
                        .param("maxAmount", "100000")
                        .param("international", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(service).getAll(
                eq(RiskLevel.HIGH),
                eq("ONLINE"),
                eq("INR"),
                eq(new BigDecimal("50000")),
                eq(new BigDecimal("100000")),
                eq(true),
                isNull(),
                isNull(),
                eq(0),
                eq(10)
        );
    }

    @Test
    void getTransactionByIdShouldReturn200() throws Exception {

        TransactionResponse transaction = new TransactionResponse(
                1L,
                new BigDecimal("85000"),
                "INR",
                "ONLINE",
                true,
                90,
                RiskLevel.HIGH,
                true,
                LocalDateTime.now()
        );

        when(service.getById(1L)).thenReturn(transaction);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(85000))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"));

        verify(service).getById(1L);
    }

    @Test
    void getNonExistingTransactionShouldReturn404() throws Exception {

        when(service.getById(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Transaction not found: 999"
                ));

        mockMvc.perform(get("/api/transactions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Transaction not found: 999"));

        verify(service).getById(999L);
    }

    @Test
    void invalidPageSizeShouldReturn400() throws Exception {

        when(service.getAll(
                any(), any(), any(), any(), any(),
                any(), any(), any(), anyInt(), anyInt()
        )).thenThrow(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid pagination parameters"
        ));

        mockMvc.perform(get("/api/transactions")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid pagination parameters"));
    }


}
