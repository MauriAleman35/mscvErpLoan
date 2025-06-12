FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR compilado (después de arreglar los errores y compilar)
COPY target/*.jar app.jar

# Configuración para la base de datos
# ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/bi_microservice
# ENV SPRING_DATASOURCE_USERNAME=postgres
# ENV SPRING_DATASOURCE_PASSWORD=1234
# ENV SPRING_LIQUIBASE_ENABLED=false

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]