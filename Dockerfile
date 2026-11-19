# ---- Etapa 1: compilar con Maven Wrapper ----
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B clean package -DskipTests

# ---- Etapa 2: imagen liviana solo con el JRE ----
FROM eclipse-temurin:25-jre

WORKDIR /app
# Sin apt-get: el wallet se descarga desde S3 en Java (OracleWalletBootstrap).
RUN useradd --system --no-create-home appuser \
 && mkdir -p /app/wallet \
 && chown appuser:appuser /app/wallet \
 && chmod 0700 /app/wallet
COPY --from=build /app/target/ms-usuarios-0.0.1-SNAPSHOT.jar /app/app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

ENV TNS_ADMIN=/app/wallet
RUN sed -i 's/\r$//' /app/docker-entrypoint.sh \
 && chmod 0555 /app/docker-entrypoint.sh
USER appuser
EXPOSE 8081

ENTRYPOINT ["/app/docker-entrypoint.sh"]
