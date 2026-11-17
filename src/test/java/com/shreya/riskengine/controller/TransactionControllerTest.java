
package com.shreya.riskengine.controller;

import com.shreya.riskengine.dto.TransactionRequest;
import com.shreya.riskengine.dto.TransactionResponse;
import com.shreya.riskengine.entity.RiskLevel;
import com.shreya.riskengine.service.TransactionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
}
