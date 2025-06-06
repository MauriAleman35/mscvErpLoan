package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Document;
import com.miempresa.erp.domain.User;
import com.miempresa.erp.repository.DocumentRepository;
import com.miempresa.erp.repository.UserRepository;
import com.miempresa.erp.services.PinataService;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DocumentResolver {

    private final Logger log = LoggerFactory.getLogger(DocumentResolver.class);

    private final DocumentRepository documentRepository;
    private final UserRepository jhiUserRepository;
    private final PinataService pinataService;

    public DocumentResolver(DocumentRepository documentRepository, UserRepository jhiUserRepository, PinataService pinataService) {
        this.documentRepository = documentRepository;
        this.jhiUserRepository = jhiUserRepository;
        this.pinataService = pinataService;
    }

    // Queries
    @QueryMapping
    public Document document(@Argument Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Document> documentsByUser(@Argument Long userId) {
        return documentRepository.findByUserId(userId);
    }

    @QueryMapping
    public String getDocumentHttpUrl(@Argument Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        return pinataService.getHttpUrl(document.getUrlFile());
    }

    // Mutation principal - ahora acepta archivo directamente
    @MutationMapping
    public Document createDocument(@Argument DocumentInput input) {
        try {
            log.info("Creando documento con tipo: {}", input.getDocumentType());

            Document document = new Document();

            // Si hay un archivo, subirlo a Pinata
            if (input.getFile() != null) {
                String ipfsUrl = pinataService.uploadFile(input.getFile(), input.getDocumentType());
                document.setUrlFile(ipfsUrl);
                log.info("Archivo subido a IPFS: {}", ipfsUrl);
            } else if (input.getUrlFile() != null) {
                document.setUrlFile(input.getUrlFile());
                log.info("Usando URL proporcionada: {}", input.getUrlFile());
            } else {
                throw new RuntimeException("Se requiere archivo o URL");
            }

            // Configurar el resto de campos
            document.setUploadDate(input.getUploadDate() != null ? input.getUploadDate() : Instant.now());
            document.setVerified(input.getVerified() != null ? input.getVerified() : false);

            // Asignar usuario
            if (input.getUserId() != null) {
                User user = jhiUserRepository.findById(input.getUserId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                document.setUser(user);
            }

            Document savedDoc = documentRepository.save(document);
            log.info("Documento guardado con ID: {}", savedDoc.getId());

            return savedDoc;
        } catch (IOException e) {
            log.error("Error al procesar documento: {}", e.getMessage());
            throw new RuntimeException("Error al procesar documento: " + e.getMessage());
        }
    }

    @MutationMapping
    public Document updateDocument(@Argument Long id, @Argument DocumentInput input) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        try {
            // Si hay nuevo archivo, actualizar URL
            if (input.getFile() != null) {
                String ipfsUrl = pinataService.uploadFile(input.getFile(), input.getDocumentType());
                document.setUrlFile(ipfsUrl);
            } else if (input.getUrlFile() != null) {
                document.setUrlFile(input.getUrlFile());
            }

            // Actualizar otros campos
            if (input.getUploadDate() != null) document.setUploadDate(input.getUploadDate());
            if (input.getVerified() != null) document.setVerified(input.getVerified());

            if (input.getUserId() != null) {
                User user = jhiUserRepository.findById(input.getUserId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                document.setUser(user);
            }

            return documentRepository.save(document);
        } catch (IOException e) {
            throw new RuntimeException("Error al actualizar documento: " + e.getMessage());
        }
    }

    @MutationMapping
    public Boolean deleteDocument(@Argument Long id) {
        try {
            documentRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //No sirve
    @MutationMapping
    public Document verifyDocument(@Argument Long id, @Argument Boolean verified) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        document.setVerified(verified);
        return documentRepository.save(document);
    }
}
