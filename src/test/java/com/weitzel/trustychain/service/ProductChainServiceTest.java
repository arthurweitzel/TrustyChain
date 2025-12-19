package com.weitzel.trustychain.service;

import com.weitzel.trustychain.actor.Actor;
import com.weitzel.trustychain.actor.ActorRepository;
import com.weitzel.trustychain.chain.ProductChain;
import com.weitzel.trustychain.chain.ProductChainRepository;
import com.weitzel.trustychain.chain.ProductChainService;
import com.weitzel.trustychain.common.exception.Exceptions;
import com.weitzel.trustychain.common.service.CryptoService;
import com.weitzel.trustychain.common.service.HashService;
import com.weitzel.trustychain.common.service.SignedTimestamp;
import com.weitzel.trustychain.common.service.TimestampService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductChainServiceTest {

        @Mock
        private ProductChainRepository productChainRepository;

        @Mock
        private ActorRepository actorRepository;

        @Mock
        private HashService hashService;

        @Mock
        private CryptoService cryptoService;

        @Mock
        private TimestampService timestampService;

        @InjectMocks
        private ProductChainService productChainService;

        private Actor testActor;
        private PublicKey testPublicKey;

        @BeforeEach
        void setUp() throws Exception {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                KeyPair keyPair = keyGen.generateKeyPair();
                testPublicKey = keyPair.getPublic();

                testActor = new Actor();
                testActor.setName("Test Actor");
                testActor.setPublicKey("-----BEGIN PUBLIC KEY-----\ntest\n-----END PUBLIC KEY-----");
        }

        @Test
        @DisplayName("Should register event successfully")
        void shouldRegisterEvent() {
                when(actorRepository.findByName("Test Actor")).thenReturn(Optional.of(testActor));
                when(productChainRepository.findTopByProductCodeOrderByCreatedAtDesc("PROD-001"))
                                .thenReturn(Optional.empty());
                when(cryptoService.loadPublicKeyFromPem(anyString())).thenReturn(testPublicKey);
                when(cryptoService.verifySignature(any(), anyString(), any())).thenReturn(true);
                when(hashService.calculateIntegrityHash(any(), any(), any(), any(), any()))
                                .thenReturn("calculatedHash123");
                when(timestampService.signTimestamp(anyString()))
                                .thenReturn(new SignedTimestamp(LocalDateTime.now(), "signature"));
                when(productChainRepository.save(any(ProductChain.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                ProductChain result = productChainService.registerEvent(
                                "Test Actor", "PROD-001", "CREATE", "metadata", "signature123");

                assertNotNull(result);
                assertEquals("PROD-001", result.getProductCode());
                assertEquals("calculatedHash123", result.getCurrentHash());
                verify(productChainRepository).save(any(ProductChain.class));
        }

        @Test
        @DisplayName("Should throw exception if actor not found")
        void shouldThrowExceptionIfActorNotFound() {
                when(actorRepository.findByName("Unknown")).thenReturn(Optional.empty());

                assertThrows(Exceptions.ActorNotFoundException.class,
                                () -> productChainService.registerEvent(
                                                "Unknown", "PROD-001", "CREATE", "meta", "sig"));
        }

        @Test
        @DisplayName("Should throw exception for invalid signature")
        void shouldThrowExceptionForInvalidSignature() {
                when(actorRepository.findByName("Test Actor")).thenReturn(Optional.of(testActor));
                when(productChainRepository.findTopByProductCodeOrderByCreatedAtDesc(anyString()))
                                .thenReturn(Optional.empty());
                when(cryptoService.loadPublicKeyFromPem(anyString())).thenReturn(testPublicKey);
                when(cryptoService.verifySignature(any(), anyString(), any())).thenReturn(false);

                assertThrows(Exceptions.InvalidSignatureException.class,
                                () -> productChainService.registerEvent(
                                                "Test Actor", "PROD-001", "CREATE", "meta", "invalidSig"));
        }

        @Test
        @DisplayName("Should link event with previous hash")
        void shouldLinkEventWithPreviousHash() {
                ProductChain previousEvent = new ProductChain();
                previousEvent.setCurrentHash("previousHash123");

                when(actorRepository.findByName("Test Actor")).thenReturn(Optional.of(testActor));
                when(productChainRepository.findTopByProductCodeOrderByCreatedAtDesc("PROD-001"))
                                .thenReturn(Optional.of(previousEvent));
                when(cryptoService.loadPublicKeyFromPem(anyString())).thenReturn(testPublicKey);
                when(cryptoService.verifySignature(any(), anyString(), any())).thenReturn(true);
                when(hashService.calculateIntegrityHash(eq("previousHash123"), any(), any(), any(), any()))
                                .thenReturn("newHash");
                when(timestampService.signTimestamp(anyString()))
                                .thenReturn(new SignedTimestamp(LocalDateTime.now(), "sig"));
                when(productChainRepository.save(any(ProductChain.class)))
                                .thenAnswer(inv -> inv.getArgument(0));

                ProductChain result = productChainService.registerEvent(
                                "Test Actor", "PROD-001", "UPDATE", "meta", "sig");

                assertEquals("previousHash123", result.getPreviousHash());
        }

        @Test
        @DisplayName("Should verify chain integrity for valid chain")
        void shouldVerifyChainIntegrity() {
                ProductChain event = new ProductChain();
                event.setActor("Test Actor");
                event.setProductCode("PROD-001");
                event.setEventType("CREATE");
                event.setMetadata("meta");
                event.setPreviousHash(null);
                event.setCurrentHash("hash123");
                event.setSignature("sig");
                event.setPublicKeySnapshot(testActor.getPublicKey());
                event.setTrustedTimestamp(LocalDateTime.now());
                event.setTimestampSignature("tsig");

                when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("PROD-001"))
                                .thenReturn(List.of(event));
                when(hashService.calculateIntegrityHash(null, "Test Actor", "PROD-001", "CREATE", "meta"))
                                .thenReturn("hash123");
                when(cryptoService.loadPublicKeyFromPem(anyString())).thenReturn(testPublicKey);
                when(cryptoService.verifySignature(any(), eq("sig"), any())).thenReturn(true);
                when(timestampService.verifyTimestamp(eq("hash123"), any())).thenReturn(true);

                boolean result = productChainService.verifyChainIntegrity("PROD-001");

                assertTrue(result);
        }

        @Test
        @DisplayName("Should return false for empty chain")
        void shouldReturnFalseForEmptyChain() {
                when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("EMPTY"))
                                .thenReturn(List.of());

                boolean result = productChainService.verifyChainIntegrity("EMPTY");

                assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for broken hash chain")
        void shouldReturnFalseForBrokenHashChain() {
                ProductChain event = new ProductChain();
                event.setPreviousHash("wrongPreviousHash"); // Should be null for first event
                event.setCurrentHash("hash123");

                when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("BROKEN"))
                                .thenReturn(List.of(event));

                boolean result = productChainService.verifyChainIntegrity("BROKEN");

                assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for hash mismatch")
        void shouldReturnFalseForHashMismatch() {
                ProductChain event = new ProductChain();
                event.setActor("Test Actor");
                event.setProductCode("PROD-001");
                event.setEventType("CREATE");
                event.setMetadata("meta");
                event.setPreviousHash(null);
                event.setCurrentHash("wrongHash");
                event.setSignature("sig");
                event.setPublicKeySnapshot(testActor.getPublicKey());

                when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("MISMATCH"))
                                .thenReturn(List.of(event));
                when(hashService.calculateIntegrityHash(null, "Test Actor", "PROD-001", "CREATE", "meta"))
                                .thenReturn("correctHash"); // Different from event hash

                boolean result = productChainService.verifyChainIntegrity("MISMATCH");

                assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for invalid signature in chain")
        void shouldReturnFalseForInvalidSignatureInChain() {
                ProductChain event = new ProductChain();
                event.setActor("Test Actor");
                event.setProductCode("PROD-001");
                event.setEventType("CREATE");
                event.setMetadata("meta");
                event.setPreviousHash(null);
                event.setCurrentHash("hash123");
                event.setSignature("invalidSig");
                event.setPublicKeySnapshot(testActor.getPublicKey());
                event.setTrustedTimestamp(LocalDateTime.now());
                event.setTimestampSignature("tsig");

                when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("INVALIDSIG"))
                                .thenReturn(List.of(event));
                when(hashService.calculateIntegrityHash(any(), any(), any(), any(), any()))
                                .thenReturn("hash123");
                when(cryptoService.loadPublicKeyFromPem(anyString())).thenReturn(testPublicKey);
                when(cryptoService.verifySignature(any(), eq("invalidSig"), any())).thenReturn(false);

                boolean result = productChainService.verifyChainIntegrity("INVALIDSIG");

                assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for invalid timestamp signature")
        void shouldReturnFalseForInvalidTimestampSignature() {
                ProductChain event = new ProductChain();
                event.setActor("Test Actor");
                event.setProductCode("PROD-001");
                event.setEventType("CREATE");
                event.setMetadata("meta");
                event.setPreviousHash(null);
                event.setCurrentHash("hash123");
                event.setSignature("sig");
                event.setPublicKeySnapshot(testActor.getPublicKey());
                event.setTrustedTimestamp(LocalDateTime.now());
                event.setTimestampSignature("invalidTsig");

                when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("INVALIDTS"))
                                .thenReturn(List.of(event));
                when(hashService.calculateIntegrityHash(any(), any(), any(), any(), any()))
                                .thenReturn("hash123");
                when(cryptoService.loadPublicKeyFromPem(anyString())).thenReturn(testPublicKey);
                when(cryptoService.verifySignature(any(), any(), any())).thenReturn(true);
                when(timestampService.verifyTimestamp(eq("hash123"), any())).thenReturn(false);

                boolean result = productChainService.verifyChainIntegrity("INVALIDTS");

                assertFalse(result);
        }

        @Test
        @DisplayName("Should verify chain with multiple events")
        void shouldVerifyChainWithMultipleEvents() {
                ProductChain event1 = new ProductChain();
                event1.setActor("Actor1");
                event1.setProductCode("MULTI");
                event1.setEventType("CREATE");
                event1.setMetadata("meta1");
                event1.setPreviousHash(null);
                event1.setCurrentHash("hash1");
                event1.setSignature("sig1");
                event1.setPublicKeySnapshot(testActor.getPublicKey());
                event1.setTrustedTimestamp(LocalDateTime.now());
                event1.setTimestampSignature("tsig1");

                ProductChain event2 = new ProductChain();
                event2.setActor("Actor2");
                event2.setProductCode("MULTI");
                event2.setEventType("UPDATE");
                event2.setMetadata("meta2");
                event2.setPreviousHash("hash1");
                event2.setCurrentHash("hash2");
                event2.setSignature("sig2");
                event2.setPublicKeySnapshot(testActor.getPublicKey());
                event2.setTrustedTimestamp(LocalDateTime.now());
                event2.setTimestampSignature("tsig2");

                when(productChainRepository.findByProductCodeOrderByCreatedAtAsc("MULTI"))
                                .thenReturn(List.of(event1, event2));
                when(hashService.calculateIntegrityHash(null, "Actor1", "MULTI", "CREATE", "meta1"))
                                .thenReturn("hash1");
                when(hashService.calculateIntegrityHash("hash1", "Actor2", "MULTI", "UPDATE", "meta2"))
                                .thenReturn("hash2");
                when(cryptoService.loadPublicKeyFromPem(anyString())).thenReturn(testPublicKey);
                when(cryptoService.verifySignature(any(), any(), any())).thenReturn(true);
                when(timestampService.verifyTimestamp(anyString(), any())).thenReturn(true);

                boolean result = productChainService.verifyChainIntegrity("MULTI");

                assertTrue(result);
        }
}
