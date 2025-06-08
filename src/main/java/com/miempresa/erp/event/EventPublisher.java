package com.miempresa.erp.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${erp.rabbitmq.exchange:erp-exchange}")
    private String exchange;

    public EventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishChange(String table, String operation, Object data) {
        try {
            // Prepara el mensaje con información adicional
            Map<String, Object> event = new HashMap<>();
            event.put("table", table);
            event.put("operation", operation);
            event.put("timestamp", LocalDateTime.now());
            event.put("id", UUID.randomUUID().toString());
            event.put("data", data);

            // Routing key formato: nombretabla.operacion (user.insert, user.update, etc)
            String routingKey = getTableName(table) + "." + operation;

            // Envía el mensaje
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception e) {
            // Log error y manejo de excepciones
            System.err.println("Error al publicar evento: " + e.getMessage());
            e.printStackTrace();  //
        }
    }

    private String getTableName(String className) {
        String tableName = className.toLowerCase();
        // Eliminar prefijo "jhi_" si existe
        if (tableName.startsWith("jhi_")) {
            tableName = tableName.substring(4);
        }
        return tableName;
    }
}
