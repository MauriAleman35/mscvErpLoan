package com.miempresa.erp.config;

import graphql.language.StringValue;
import graphql.language.Value;
import graphql.scalars.ExtendedScalars;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class GraphQlConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> {
            // Registrar escalares para fechas y números
            wiringBuilder.scalar(ExtendedScalars.Date);
            wiringBuilder.scalar(ExtendedScalars.DateTime);
            wiringBuilder.scalar(ExtendedScalars.GraphQLBigDecimal);

            // Registrar escalar para Upload
            wiringBuilder.scalar(
                GraphQLScalarType.newScalar()
                    .name("Upload")
                    .description("Un tipo para subir archivos")
                    .coercing(new UploadCoercing())
                    .build()
            );
            // Agregar aquí cualquier otra configuración que tuvieras en el anterior GraphQlConfig
        };
    }

    // Clase interna para manejar la subida de archivos
    static class UploadCoercing implements Coercing<MultipartFile, String> {

        @Override
        public String serialize(Object dataFetcherResult) {
            throw new CoercingSerializeException("Upload no puede ser serializado");
        }

        @Override
        public MultipartFile parseValue(Object input) {
            if (input instanceof MultipartFile) {
                return (MultipartFile) input;
            }
            throw new CoercingParseValueException("Input no es un MultipartFile");
        }

        @Override
        public MultipartFile parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return null; // No podemos convertir un literal a un archivo
            }
            throw new CoercingParseLiteralException("Input no puede ser parseado como MultipartFile");
        }

        @Override
        public Value<?> valueToLiteral(Object input) {
            return null; // No implementado
        }

        @Bean
        public GraphQLScalarType dateTimeScalar() {
            return ExtendedScalars.DateTime; // Maneja automáticamente Instant, Date, etc.
        }
    }
}
