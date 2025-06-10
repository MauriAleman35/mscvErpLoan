package com.miempresa.erp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class proxyPinata {

    private final Logger log = LoggerFactory.getLogger(proxyPinata.class);
    private final RestTemplate restTemplate;

    public proxyPinata() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/proxy-image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("url") String imageUrl) {
        log.debug("Solicitando proxy para imagen: {}", imageUrl);

        try {
            // Configurar headers para la solicitud
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG));

            // Realizar la solicitud a Pinata
            ResponseEntity<byte[]> response = restTemplate.exchange(imageUrl, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

            // Configurar headers para la respuesta
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.IMAGE_JPEG);
            responseHeaders.setCacheControl(CacheControl.maxAge(java.time.Duration.ofDays(1)));

            return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error al obtener imagen de {}: {}", imageUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
