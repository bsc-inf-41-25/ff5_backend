package com.ticketmaster.ticketmaster.ticket;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeService();

    @Test
    void generatesValidPngForTicketCode() throws IOException {
        byte[] png = qrCodeService.generateQrCode(
                "550e8400-e29b-41d4-a716-446655440000");

        assertNotNull(png);
        assertTrue(png.length > 0);

        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertNotNull(image, "Output should be a readable PNG image.");
        assertEquals(300, image.getWidth());
        assertEquals(300, image.getHeight());
    }

    @Test
    void rejectsNullText() {
        assertThrows(IllegalArgumentException.class,
                () -> qrCodeService.generateQrCode(null));
    }


    @Test
    void rejectsBlankText() {
        assertThrows(IllegalArgumentException.class,
                () -> qrCodeService.generateQrCode("   "));
    }

    @Test
    void supportsLongTicketCodes() {
        String longCode = "TICKET-" + "A".repeat(500);

        byte[] png = assertDoesNotThrow(
                () -> qrCodeService.generateQrCode(longCode));

        assertTrue(png.length > 0);
    }
}
