package com.miempresa.erp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miempresa.erp.config.PinataConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PinataService {

    private final PinataConfig pinataConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PinataService(PinataConfig pinataConfig) {
        this.pinataConfig = pinataConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String uploadFile(MultipartFile file, String name) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("pinata_api_key", pinataConfig.getApiKey());
        headers.add("pinata_secret_api_key", pinataConfig.getApiSecret());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Metadatos para Pinata
        Map<String, Object> pinataMetadata = new HashMap<>();
        pinataMetadata.put("name", name);

        Map<String, Object> pinataOptions = new HashMap<>();
        pinataOptions.put("cidVersion", 1);

        Map<String, Object> pinataContent = new HashMap<>();
        pinataContent.put("pinataMetadata", pinataMetadata);
        pinataContent.put("pinataOptions", pinataOptions);

        body.add("file", file.getResource());
        body.add("pinataMetadata", objectMapper.writeValueAsString(pinataMetadata));
        body.add("pinataOptions", objectMapper.writeValueAsString(pinataOptions));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            pinataConfig.getBaseUrl() + "/pinning/pinFileToIPFS",
            requestEntity,
            Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("IpfsHash")) {
            String ipfsHash = (String) response.getBody().get("IpfsHash");
            return "ipfs://" + ipfsHash;
        } else {
            throw new IOException("Error al subir archivo a IPFS: " + response.getBody());
        }
    }

    public String getHttpUrl(String ipfsUrl) {
        if (ipfsUrl.startsWith("ipfs://")) {
            String hash = ipfsUrl.substring(7);
            return "https://gateway.pinata.cloud/ipfs/" + hash;
        }
        return ipfsUrl;
    }
}
