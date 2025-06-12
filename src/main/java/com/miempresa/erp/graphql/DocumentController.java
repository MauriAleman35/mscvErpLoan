package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Document;
import com.miempresa.erp.domain.MonthlyPayment;
import com.miempresa.erp.domain.User;
import com.miempresa.erp.repository.DocumentRepository;
import com.miempresa.erp.repository.MonthlyPaymentRepository;
import com.miempresa.erp.repository.UserRepository;
import com.miempresa.erp.services.PinataService;
import com.miempresa.erp.services.RekognitionService;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final RekognitionService rekognitionService;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final PinataService pinataService;
    private final MonthlyPaymentRepository monthlyPaymentRepository;
    private final com.miempresa.erp.services.SimpleTextractService textractService;

    public DocumentController(
        RekognitionService rekognitionService,
        DocumentRepository documentRepository,
        UserRepository userRepository,
        PinataService pinataService,
        MonthlyPaymentRepository monthlyPaymentRepository,
        com.miempresa.erp.services.SimpleTextractService textractService
    ) {
        this.rekognitionService = rekognitionService;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.pinataService = pinataService;
        this.monthlyPaymentRepository = monthlyPaymentRepository;
        this.textractService = textractService;
    }

    @PostMapping("/documents/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam("documentType") String documentType,
        @RequestParam("userId") Long userId
    ) {
        try {
            log.info("Recibiendo archivo: {} de tipo {}, tamaño: {}", file.getOriginalFilename(), documentType, file.getSize());

            // Subir a Pinata
            String ipfsUrl = pinataService.uploadFile(file, documentType);

            String httpUrl = pinataService.getHttpUrl(ipfsUrl);
            // Verificar que se obtuvo una URL válida
            // Crear documento
            Document document = new Document();
            document.setUrlFile(httpUrl); // Guarda la URL ipfs://hash
            document.setUploadDate(Instant.now());
            document.setVerified(false);

            // Asignar usuario
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            document.setUser(user);

            Document savedDoc = documentRepository.save(document);
            log.info("Documento guardado con ID: {}", savedDoc.getId());

            // Convertir a URL HTTP para visualización

            // Respuesta con información completa
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedDoc.getId());
            response.put("urlFile", savedDoc.getUrlFile()); // URL original ipfs://hash
            response.put("httpUrl", httpUrl); // URL HTTP para visualización
            response.put("uploadDate", savedDoc.getUploadDate());
            response.put("verified", savedDoc.getVerified());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Error al subir documento: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    //metodo para verificar identidad de Prestatario
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/verify-identity")
    public ResponseEntity<Map<String, Object>> verifyIdentity(
        @RequestParam("documentImage") MultipartFile documentImage,
        @RequestParam("selfieImage") MultipartFile selfieImage,
        @RequestParam("userId") Long userId
    ) {
        log.info("Iniciando verificación de identidad para usuario ID: {}", userId);

        try {
            Optional<User> findUser = userRepository.findById(userId);
            if (findUser.isEmpty()) {
                log.error("Usuario no encontrado con ID: {}", userId);
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
            }
            if (findUser.get().getIdentityVerified()) {
                log.info("Usuario ya verificado: {}", userId);
                return ResponseEntity.ok(Map.of("message", "Usuario ya verificado"));
            }
            // 1. Verificar identidad con AWS Rekognition
            var result = rekognitionService.verifyIdentity(documentImage, selfieImage, findUser.get());

            // 2. Si la verificación es exitosa, guardar imágenes y actualizar estado
            Map<String, Object> response = new HashMap<>();
            response.put("verified", result.isVerified());
            response.put("similarity", result.getSimilarity());
            response.put("message", result.getMessage());

            if (result.isVerified()) {
                // Actualizar usuario como verificado
                User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                user.setIdentityVerified(true);
                userRepository.save(user);

                // Guardar documentos en Pinata/IPFS
                Document docAnverso = saveDocument(documentImage, "ID_FRONT", user);
                Document docSelfie = saveDocument(selfieImage, "SELFIE", user);

                // Añadir IDs de documentos a la respuesta
                response.put("documentId", docAnverso.getId());
                response.put("selfieId", docSelfie.getId());
                response.put("userVerified", true);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en el proceso de verificación: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("verified", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    private Document saveDocument(MultipartFile file, String type, User user) throws IOException {
        // Subir a Pinata/IPFS
        String ipfsUrl = pinataService.uploadFile(file, type);
        String httpUrl = pinataService.getHttpUrl(ipfsUrl);
        // Crear documento
        Document document = new Document();

        document.setUrlFile(httpUrl);
        document.setUploadDate(Instant.now());
        document.setVerified(true); // Ya verificado por Rekognition
        document.setUser(user);

        return documentRepository.save(document);
    }

    @GetMapping("/documents/{id}/url")
    public ResponseEntity<Map<String, String>> getDocumentUrl(@PathVariable Long id) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        String httpUrl = pinataService.getHttpUrl(document.getUrlFile());

        Map<String, String> response = new HashMap<>();
        response.put("urlFile", document.getUrlFile()); // URL original ipfs://hash
        response.put("httpUrl", httpUrl); // URL HTTP para visualización

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/verify-address")
    public ResponseEntity<Map<String, Object>> verifyAddress(
        @RequestParam("document") MultipartFile document,
        @RequestParam("userId") Long userId
    ) {
        try {
            // Obtener usuario y dirección registrada
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            String firstName = user.getName();
            String lastName = user.getLastName();

            // Extraer texto del documento con Textract
            String extractedText = textractService.extractText(document);

            // Comparación simple
            boolean isVerified = textractService.verifyUserName(extractedText, firstName, lastName);

            // Guardar documento en IPFS si es necesario
            String documentUrl = pinataService.uploadFile(document, "ADDRESS_PROOF");

            // Crear documento en la base de datos
            Document addressDoc = new Document();

            addressDoc.setUrlFile(documentUrl);
            addressDoc.setUploadDate(Instant.now());
            addressDoc.setVerified(isVerified);
            addressDoc.setUser(user);
            documentRepository.save(addressDoc);

            // Si está verificado, actualizar usuario
            if (isVerified) {
                user.setAddressVerified(true); // Asegúrate que tu modelo User tenga este campo
                userRepository.save(user);
            }

            // Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("verified", isVerified);
            response.put("documentId", addressDoc.getId());
            response.put("extractedText", extractedText); // Solo para depuración

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("verified", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/pay-monthly-payment")
    @Transactional
    public ResponseEntity<?> payMonthlyPayment(@RequestParam("file") MultipartFile file, @RequestParam("paymentId") Long paymentId) {
        try {
            // 1. Obtener el pago mensual
            MonthlyPayment payment = monthlyPaymentRepository
                .findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago mensual no encontrado"));

            // 2. Verificar que el pago esté pendiente
            if (!"pendiente".equals(payment.getPaymentStatus())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Este pago ya no está pendiente");
                return ResponseEntity.badRequest().body(error);
            }

            // 3. Subir el comprobante a Pinata
            String fileName = "comprobante_" + paymentId + "_" + System.currentTimeMillis();
            String ipfsUrl = pinataService.uploadFile(file, fileName);
            String httpUrl = pinataService.getHttpUrl(ipfsUrl);

            // 4. Actualizar el pago
            payment.setComprobantFile(httpUrl);
            payment.setPaymentDate(Instant.now());
            payment.setPaymentStatus("pendiente");
            payment.setBorrowVerified(true);
            payment.setPartnerVerified(false);

            // 5. Calcular días de retraso si aplica
            Instant dueDate = payment.getDueDate();
            if (dueDate != null && Instant.now().isAfter(dueDate)) {
                long daysLate = java.time.Duration.between(dueDate, Instant.now()).toDays();
                payment.setDaysLate((int) daysLate);

                // 6. Calcular penalidad si hay retraso
                if (daysLate > 0) {
                    BigDecimal penaltyRate = new BigDecimal("0.01"); // 1% por día
                    BigDecimal amount = payment.getLoan().getOffer().getMonthlyPayment();
                    BigDecimal penalty = amount.multiply(penaltyRate).multiply(new BigDecimal(daysLate));
                    payment.setPenaltyAmount(penalty);
                }
            }

            // 7. Guardar el pago actualizado
            monthlyPaymentRepository.save(payment);

            // 8. Preparar respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentId", payment.getId());
            response.put("comprobantUrl", httpUrl);
            response.put("status", payment.getPaymentStatus());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al subir el comprobante: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
