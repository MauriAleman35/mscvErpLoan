package com.miempresa.erp.services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RekognitionService {

    private final Logger log = LoggerFactory.getLogger(RekognitionService.class);

    private final AmazonRekognition rekognitionClient;

    public RekognitionService(
        @Value("${aws.access.key.id}") String accessKey,
        @Value("${aws.secret.access.key}") String secretKey,
        @Value("${aws.region}") String region
    ) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.rekognitionClient = AmazonRekognitionClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.fromName(region))
            .build();
    }

    /**
     * Compara una imagen de documento con una selfie para verificar si es la misma persona
     * @return VerificationResult con resultado y porcentaje de similitud
     */
    public VerificationResult verifyIdentity(MultipartFile documentImage, MultipartFile selfieImage) {
        try {
            // Detectar cara en documento
            Image docImage = convertToAwsImage(documentImage);
            DetectFacesRequest docRequest = new DetectFacesRequest().withImage(docImage).withAttributes("ALL");

            DetectFacesResult docFacesResult = rekognitionClient.detectFaces(docRequest);

            if (docFacesResult.getFaceDetails().isEmpty()) {
                log.warn("No se detectó rostro en la imagen del documento");
                return new VerificationResult(false, 0.0f, "No se detectó rostro en la imagen del documento");
            }

            // Detectar cara en selfie
            Image selfImage = convertToAwsImage(selfieImage);
            DetectFacesRequest selfieRequest = new DetectFacesRequest().withImage(selfImage).withAttributes("ALL");

            DetectFacesResult selfieFacesResult = rekognitionClient.detectFaces(selfieRequest);

            if (selfieFacesResult.getFaceDetails().isEmpty()) {
                log.warn("No se detectó rostro en la selfie");
                return new VerificationResult(false, 0.0f, "No se detectó rostro en la selfie");
            }

            // Comparar rostros
            CompareFacesRequest compareRequest = new CompareFacesRequest()
                .withSourceImage(selfImage)
                .withTargetImage(docImage)
                .withSimilarityThreshold(70F);

            CompareFacesResult compareResult = rekognitionClient.compareFaces(compareRequest);

            if (compareResult.getFaceMatches().isEmpty()) {
                log.warn("Los rostros no coinciden");
                return new VerificationResult(false, 0.0f, "Los rostros no coinciden");
            }

            Float similarity = compareResult.getFaceMatches().get(0).getSimilarity();
            boolean isVerified = similarity >= 80.0f;

            log.info("Verificación completada: coincidencia {}% - {}", similarity, isVerified ? "VERIFICADO" : "RECHAZADO");

            return new VerificationResult(
                isVerified,
                similarity,
                isVerified ? "Verificación exitosa" : "Verificación fallida: similitud insuficiente"
            );
        } catch (Exception e) {
            log.error("Error al verificar identidad: {}", e.getMessage(), e);
            return new VerificationResult(false, 0.0f, "Error en el proceso de verificación: " + e.getMessage());
        }
    }

    private Image convertToAwsImage(MultipartFile file) throws IOException {
        ByteBuffer imageBytes = ByteBuffer.wrap(file.getBytes());
        return new Image().withBytes(imageBytes);
    }

    // Clase para resultados de verificación
    public static class VerificationResult {

        private final boolean verified;
        private final float similarity;
        private final String message;

        public VerificationResult(boolean verified, float similarity, String message) {
            this.verified = verified;
            this.similarity = similarity;
            this.message = message;
        }

        // Getters
        public boolean isVerified() {
            return verified;
        }

        public float getSimilarity() {
            return similarity;
        }

        public String getMessage() {
            return message;
        }
    }
}
