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

Este servicio usa Oracle Autonomous DB (schema `FORO_APP`) con tablas `ROL` y `USUARIO`. El schema y los datos iniciales se crean aparte (entrega del curso / Database Actions); este repo solo necesita la conexión.

1. Descarga el **Wallet** de Oracle Cloud y colócalo en una ruta **sin espacios** (o crea un symlink):
   ```bash
   mkdir -p ~/oracle_wallets
   ln -sfn "/ruta/al/Wallet_XXXXXXXX" ~/oracle_wallets/foro
   ```
2. Copia la plantilla de propiedades y ajusta usuario, password y alias TNS si corresponde:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
3. Verifica que `jwt.secret` coincida con el de [ms-foro](https://github.com/TriniCorvalan/ms-foro) si vas a usar ambos servicios.

`application.properties` no se versiona (está en `.gitignore`).

## Ejecutar

```bash
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```

## Usuarios de ejemplo (seed)

| Email | Password | Rol |
|-------|----------|-----|
| admin@foro.cl | Admin123! | ADMIN |
| juan@foro.cl | User123! | USUARIO |
| ana@foro.cl | User123! | USUARIO |
