package com.miempresa.erp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GraphQLSecurityFilter extends OncePerRequestFilter {

    // Lista de operaciones que no requieren autenticación
    private static final Set<String> PUBLIC_OPERATIONS = new HashSet<>(
        Arrays.asList(
            "login", // Login
            "createUser", // Crear usuario
            "updateUser", // Actualizar usuario
            "deleteUser", // Eliminar usuario
            "user", // Consulta usuario
            "users", // Listar usuarios
            "userByEmail",
            "createOffer" // Buscar usuario por email
        )
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        // Solo aplica a /graphql y POST requests
        if ("/graphql".equals(request.getRequestURI()) && "POST".equals(request.getMethod())) {
            // Crear un wrapper para poder leer el cuerpo múltiples veces
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

            // Leer el cuerpo de la solicitud
            String body = new String(cachedRequest.getCachedBody());

            // Verificar si contiene operaciones públicas
            boolean isPublicOperation = isPublicOperation(body);

            if (!isPublicOperation) {
                // Verificar autenticación solo para operaciones no públicas
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"errors\":[{\"message\":\"Autenticación requerida para acceder a esta operación\"}]}");
                    return;
                }
            }

            // Continuar con la cadena de filtros usando el request cacheado
            filterChain.doFilter(cachedRequest, response);
        } else {
            // Para otras rutas, solo pasar la solicitud original
            filterChain.doFilter(request, response);
        }
    }

    private boolean isPublicOperation(String body) {
        // Verificar si es una operación pública basada en análisis del cuerpo
        for (String operation : PUBLIC_OPERATIONS) {
            // Verificar diferentes formatos de consulta GraphQL
            if (
                body.contains("\"operationName\":\"" + operation + "\"") ||
                body.contains("\"query\":\"mutation " + operation) ||
                body.contains("\"query\":\"query " + operation) ||
                // Consulta anónima con operación directa (mutation { login(...) })
                body.contains("mutation { " + operation) ||
                body.contains("mutation {" + operation) ||
                body.contains("query { " + operation) ||
                body.contains("query {" + operation) ||
                // Consulta con espacios y saltos de línea
                body.contains("mutation {\\n  " + operation) ||
                body.contains("query {\\n  " + operation)
            ) {
                return true;
            }
        }

        return false;
    }
}

// Clase auxiliar para poder leer el cuerpo de la solicitud múltiples veces
class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        InputStream requestInputStream = request.getInputStream();
        this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    public byte[] getCachedBody() {
        return cachedBody;
    }
}

class CachedBodyServletInputStream extends ServletInputStream {

    private ByteArrayInputStream inputStream;

    public CachedBodyServletInputStream(byte[] cachedBody) {
        this.inputStream = new ByteArrayInputStream(cachedBody);
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public boolean isFinished() {
        return inputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }
}
