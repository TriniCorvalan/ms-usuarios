# ms-usuarios

Microservicio de **usuarios e inicio de sesión** del Sistema de Foros en Línea (DUOC Full Stack III — Exp 1 Semana 2).

- Puerto: **8081**
- Stack: Spring Boot 4, Spring Data JPA, Spring Security + JWT, Oracle Cloud
- Roles: `ADMIN`, `USUARIO`

## Endpoints

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/auth/login` | Público | Login → JWT |
| POST | `/auth/register` | Público | Registro (rol USUARIO) |
| GET | `/usuarios` | ADMIN | Listar |
| GET | `/usuarios/{id}` | Propio o ADMIN | Detalle |
| PUT | `/usuarios/{id}` | Propio o ADMIN | Actualizar |
| DELETE | `/usuarios/{id}` | ADMIN | Eliminar |
| GET | `/roles` | ADMIN | Listar roles |

## Configuración

1. Ejecutar `../sql/01_schema_foro.sql` como ADMIN en Oracle.
2. Symlink del wallet (sin espacios en la ruta):
   ```bash
   mkdir -p ~/oracle_wallets
   ln -sfn "/ruta/a/Wallet_TG243BR3UDTANBR0" ~/oracle_wallets/foro
   ```
3. Copiar propiedades locales:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```

## Ejecutar

```bash
export JAVA_HOME=$(/usr/libexec/java_home)
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```

## Credenciales seed

| Email | Password | Rol |
|-------|----------|-----|
| admin@foro.cl | Admin123! | ADMIN |
| juan@foro.cl | User123! | USUARIO |
