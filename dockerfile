# --- Etapa de compilación ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom y código fuente
COPY pom.xml .
COPY src ./src

# Compilar en Java 17
RUN mvn clean package -DskipTests


# --- Imagen final (solo JRE 17) ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia el jar generado en la etapa previa
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
