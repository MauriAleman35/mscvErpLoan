package com.miempresa.erp.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SimpleTextractService {

    private final Logger log = LoggerFactory.getLogger(SimpleTextractService.class);

    private final AmazonTextract textractClient;

    public SimpleTextractService(
        @Value("${aws.access.key.id.textract}") String accessKey,
        @Value("${aws.secret.access.key.textract}") String secretKey,
        @Value("${aws.region}") String region
    ) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.textractClient = AmazonTextractClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.fromName(region))
            .build();
    }

    /**
     * Extrae todo el texto de un documento utilizando AWS Textract
     * @return String con todo el texto extraído
     */
    public String extractText(MultipartFile document) {
        try {
            // Convertir a formato AWS
            ByteBuffer imageBytes = ByteBuffer.wrap(document.getBytes());
            Document awsDocument = new Document().withBytes(imageBytes);

            // Llamar a Textract para extraer texto
            DetectDocumentTextRequest request = new DetectDocumentTextRequest().withDocument(awsDocument);

            DetectDocumentTextResult result = textractClient.detectDocumentText(request);

            // Unir todo el texto en un solo String
            String extractedText = result
                .getBlocks()
                .stream()
                .filter(block -> block.getBlockType().equals("LINE") || block.getBlockType().equals("WORD"))
                .map(block -> block.getText())
                .collect(Collectors.joining(" "));

            log.info("Texto extraído: {}", extractedText);
            return extractedText;
        } catch (Exception e) {
            log.error("Error al extraer texto: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Verifica si una dirección registrada aparece en el texto extraído
     */
    public boolean verifyAddress(String extractedText, String registeredAddress) {
        if (extractedText == null || registeredAddress == null || extractedText.isEmpty() || registeredAddress.isEmpty()) {
            return false;
        }

        // Normalización básica
        String normalizedText = extractedText.toLowerCase().replaceAll("[,.:;]", " ");
        String normalizedAddress = registeredAddress.toLowerCase().replaceAll("[,.:;]", " ");

        // Verificar si partes importantes de la dirección están en el texto
        String[] addressParts = normalizedAddress.split("\\s+");
        int matchCount = 0;

        for (String part : addressParts) {
            // Ignorar palabras muy cortas o comunes
            if (part.length() <= 2 || part.equals("de") || part.equals("la") || part.equals("el")) {
                continue;
            }

            if (normalizedText.contains(part)) {
                matchCount++;
            }
        }

        // Si al menos el 60% de las palabras importantes coinciden
        double matchPercentage = ((double) matchCount / addressParts.length) * 100;
        log.info("Coincidencia de dirección: {}%", matchPercentage);

        return matchPercentage >= 60.0;
    }

    public boolean verifyUserName(String extractedText, String userName, String lastName) {
        if (extractedText == null || userName == null || extractedText.isEmpty() || userName.isEmpty()) {
            return false;
        }

        // Normalización básica
        String normalizedText = extractedText.toLowerCase();
        String normalizedName = userName.toLowerCase();
        String normalizedLastName = lastName != null ? lastName.toLowerCase() : "";

        // Verificar si el nombre aparece en el texto
        boolean nameFound = normalizedText.contains(normalizedName);
        boolean lastNameFound = normalizedLastName.isEmpty() ? true : normalizedText.contains(normalizedLastName);

        log.info("Verificación de nombre: {} - Nombre encontrado: {}, Apellido encontrado: {}", userName, nameFound, lastNameFound);

        return nameFound && lastNameFound;
    }
}
