# Usamos una imagen ligera de Java 21 (o la versión que uses)
FROM eclipse-temurin:21-jdk-alpine

# Creamos un directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el ejecutable .jar ya compilado de Spring Boot al contenedor
COPY target/PolleriaLatina-0.0.1-SNAPSHOT.jar app.jar

# Exponemos un puerto dinámico (Vital para la nube)
ENV PORT=8080
EXPOSE $PORT

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]