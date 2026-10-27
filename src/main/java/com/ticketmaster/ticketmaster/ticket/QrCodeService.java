package com.ticketmaster.ticketmaster.ticket;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@Service
public class QrCodeService {

    public byte[] generateQrCode(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "QR code text must not be blank.");
        }

        Map<EncodeHintType, Object> hints =
                new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION,
                ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            var matrix = new QRCodeWriter().encode(
                    text, BarcodeFormat.QR_CODE, 300, 300, hints);

            try (ByteArrayOutputStream output =
                         new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(
                        matrix, "PNG", output);
                return output.toByteArray();
            }
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException(
                    "Could not generate the ticket QR code.",
                    exception);
        }
    }
}
