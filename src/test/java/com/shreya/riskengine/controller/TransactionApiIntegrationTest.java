
package com.shreya.riskengine.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreya.riskengine.dto.TransactionRequest;
import com.shreya.riskengine.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:riskengine-integration;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TransactionApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createAndRetrieveTransactionShouldWorkEndToEnd()
            throws Exception {

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("85000"),
                "INR",
                "ONLINE",
                true
        );

        String response = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riskScore").value(90))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.flagged").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdTransaction = objectMapper.readTree(response);
        long id = createdTransaction.get("id").asLong();

        mockMvc.perform(get("/api/transactions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value(85000))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"));
    }

    @Test
    void multipleFiltersShouldReturnOnlyMatchingDatabaseRecords()
            throws Exception {

        createTransaction("85000", "INR", "ONLINE", true);
        createTransaction("10000", "INR", "BRANCH", false);
        createTransaction("60000", "USD", "ONLINE", false);

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
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(85000))
                .andExpect(jsonPath("$.content[0].currency").value("INR"))
                .andExpect(jsonPath("$.content[0].riskLevel").value("HIGH"));
    }

    private void createTransaction(
            String amount,
            String currency,
            String channel,
            boolean international) throws Exception {

        TransactionRequest request = new TransactionRequest(
                new BigDecimal(amount),
                currency,
                channel,
                international
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
