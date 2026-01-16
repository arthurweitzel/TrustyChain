package com.weitzel.trustychain.controller;

import com.weitzel.trustychain.auth.JwtAuthenticationFilter;
import com.weitzel.trustychain.auth.JwtService;
import com.weitzel.trustychain.chain.ProductChain;
import com.weitzel.trustychain.chain.ProductChainController;
import com.weitzel.trustychain.chain.ProductChainRepository;
import com.weitzel.trustychain.chain.ProductChainService;
import com.weitzel.trustychain.chain.TrackingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductChainController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductChainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductChainService productChainService;

    @MockBean
    private ProductChainRepository productChainRepository;

    @MockBean
    private TrackingService trackingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Should register event")
    void shouldRegisterEvent() throws Exception {
        ProductChain event = new ProductChain();
        event.setActor("Test Actor");
        event.setProductCode("PROD-001");
        event.setEventType("CREATE");
        event.setMetadata("metadata");
        event.setCurrentHash("hash123");
        event.setTrustedTimestamp(LocalDateTime.now());
        event.setTimestampSignature("sig");

        when(productChainService.registerEvent(any(), any(), any(), any(), any()))
                .thenReturn(event);

        String requestBody = """
                {
                    "actor": "Test Actor",
                    "productCode": "PROD-001",
                    "eventType": "CREATE",
                    "metadata": "metadata",
                    "signature": "signatureBase64"
                }
                """;

        mockMvc.perform(post("/api/product-chain/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode").value("PROD-001"))
                .andExpect(jsonPath("$.currentHash").value("hash123"));
    }

    @Test
    @DisplayName("Should get product history")
    void shouldGetProductHistory() throws Exception {
        ProductChain event = new ProductChain();
        event.setActor("Test Actor");
        event.setProductCode("PROD-001");
        event.setEventType("CREATE");
        event.setMetadata("metadata");
        event.setCurrentHash("hash123");
        event.setTrustedTimestamp(LocalDateTime.now());

        when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("PROD-001"))
                .thenReturn(List.of(event));
        when(productChainService.verifyChainIntegrity("PROD-001")).thenReturn(true);
        when(trackingService.generateQRCodeUrl("PROD-001"))
                .thenReturn("http://localhost:8080/api/product-chain/PROD-001/qr");

        mockMvc.perform(get("/api/product-chain/PROD-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode").value("PROD-001"))
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.events").isArray());
    }

    @Test
    @DisplayName("Should verify chain integrity")
    void shouldVerifyChainIntegrity() throws Exception {
        ProductChain event = new ProductChain();
        when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("PROD-001"))
                .thenReturn(List.of(event));
        when(productChainService.verifyChainIntegrity("PROD-001")).thenReturn(true);

        mockMvc.perform(get("/api/product-chain/PROD-001/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode").value("PROD-001"))
                .andExpect(jsonPath("$.isValid").value(true));
    }

    @Test
    @DisplayName("Should return not found for empty product")
    void shouldReturnNotFoundForEmptyProduct() throws Exception {
        when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("UNKNOWN"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/product-chain/UNKNOWN"))
                .andExpect(status().isNotFound());
    }
}