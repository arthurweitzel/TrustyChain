package com.weitzel.trustychain.tracking;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class TrackingService {

    @Value("${trustychain.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final int DEFAULT_QR_SIZE = 300;

    public byte[] generateQRCode(String productCode, int width, int height) {
        try {
            String trackingUrl = generateTrackingUrl(productCode);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(trackingUrl, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code for product: " + productCode, e);
        }
    }

    public byte[] generateQRCode(String productCode) {
        return generateQRCode(productCode, DEFAULT_QR_SIZE, DEFAULT_QR_SIZE);
    }

    public String generateTrackingUrl(String productCode) {
        return baseUrl + "/api/tracking/" + productCode;
    }

    public String generateQRCodeUrl(String productCode) {
        return baseUrl + "/api/tracking/" + productCode + "/qr";
    }
}
