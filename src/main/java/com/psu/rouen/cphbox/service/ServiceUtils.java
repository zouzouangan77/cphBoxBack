package com.psu.rouen.cphbox.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import org.apache.commons.lang3.RandomStringUtils;

public final class ServiceUtils {

    public static String generateReference() {
        return RandomStringUtils.randomAlphanumeric(5).toUpperCase() + "-" + Instant.now().getEpochSecond();
    }

    public static String getQRCodeImageBase64(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        //MatrixToImageConfig con = new MatrixToImageConfig( 0xFFFFFFFF , 0xFFFFC041 ) ;

        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pngData);
    }

    public static int toInt(Integer value) {
        if (value != null) {
            return value.intValue();
        } else {
            return 0;
        }
    }
}
