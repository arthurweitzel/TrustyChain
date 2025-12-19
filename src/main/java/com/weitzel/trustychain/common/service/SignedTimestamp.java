package com.weitzel.trustychain.common.service;

import java.time.LocalDateTime;

public record SignedTimestamp(
        LocalDateTime timestamp,
        String signature // base64 encoded rsa signature
) {}
