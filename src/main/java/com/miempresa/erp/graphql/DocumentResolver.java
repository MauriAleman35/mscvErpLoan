package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.Document;
import com.miempresa.erp.domain.User;
import com.miempresa.erp.repository.DocumentRepository;
import com.miempresa.erp.repository.UserRepository;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DocumentResolver {

    private final DocumentRepository documentRepository;
    private final UserRepository jhiUserRepository;

    public DocumentResolver(DocumentRepository documentRepository, UserRepository jhiUserRepository) {
        this.documentRepository = documentRepository;
        this.jhiUserRepository = jhiUserRepository;
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

    // Mutations
    @MutationMapping
    public Document createDocument(@Argument DocumentInput input) {
        Document document = new Document();
        mapDocumentInputToEntity(input, document);

        if (input.getUserId() != null) {
            User user = jhiUserRepository.findById(input.getUserId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            document.setUser(user);
        }

        return documentRepository.save(document);
    }

    @MutationMapping
    public Document updateDocument(@Argument Long id, @Argument DocumentInput input) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        mapDocumentInputToEntity(input, document);

        if (input.getUserId() != null) {
            User user = jhiUserRepository.findById(input.getUserId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            document.setUser(user);
        }

        return documentRepository.save(document);
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

    @MutationMapping
    public Document verifyDocument(@Argument Long id, @Argument Boolean verified) {
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        document.setVerified(verified);
        return documentRepository.save(document);
    }

    // Helper method
    private void mapDocumentInputToEntity(DocumentInput input, Document document) {
        if (input.getUrlFile() != null) document.setUrlFile(input.getUrlFile());
        if (input.getUploadDate() != null) document.setUploadDate(input.getUploadDate());
        if (input.getVerified() != null) document.setVerified(input.getVerified());
    }
}
