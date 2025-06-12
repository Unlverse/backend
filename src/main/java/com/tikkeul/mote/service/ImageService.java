package com.tikkeul.mote.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final WebClient webClient;

    public Map<String, Object> extractGpsInfo(File imageFile) throws IOException, ImageReadException {
        var metadata = Imaging.getMetadata(imageFile);
        if (metadata instanceof JpegImageMetadata jpegMetadata) {
            TiffField latRef = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
            TiffField lonRef = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
            TiffField latField = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
            TiffField lonField = jpegMetadata.findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);

            if (latField != null && lonField != null) {
                double latitude = convertToDegrees(latField.getValue(), latRef.getStringValue());
                double longitude = convertToDegrees(lonField.getValue(), lonRef.getStringValue());
                return Map.of("latitude", latitude, "longitude", longitude);
            }
        }
        throw new IllegalStateException("GPS 정보를 찾을 수 없습니다.");
    }

    private double convertToDegrees(Object value, String ref) {
        RationalNumber[] numbers = (RationalNumber[]) value;
        double deg = numbers[0].doubleValue();
        double min = numbers[1].doubleValue();
        double sec = numbers[2].doubleValue();
        double result = deg + (min / 60.0) + (sec / 3600.0);
        return ("S".equalsIgnoreCase(ref) || "W".equalsIgnoreCase(ref)) ? -result : result;
    }

    public String sendToOcrServer(File imageFile) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

        return webClient.post()
                .uri("/ocr")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("file", new ByteArrayResource(imageBytes) {
                    @Override
                    public String getFilename() {
                        return imageFile.getName();
                    }
                }))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}