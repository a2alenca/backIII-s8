# Backend III - Semana 8

Proyecto de microservicios para Banco XYZ, desarrollado con Spring Boot, OAuth2/JWT, Docker, PostgreSQL, Resilience4j y Apache Kafka.
Alumno: Camilo Alarcon Araya
## contenedores
- **auth-server**: Servidor de autorización OAuth2 (`puerto 9000`).
- **core-bank**: Núcleo bancario y procesamiento Batch (`puerto 8080`).
- **bff-web**: BFF para canal web (`puerto 8081`).
- **bff-mobile**: BFF para canal móvil (`puerto 8082`).
- **bff-kiosk**: BFF para cajero/kiosco (`puerto 8083`).
- **postgres-banco**: Base de datos relacional PostgreSQL (`puerto 5432`).
- **broker**: Apache Kafka (`puerto 9092`).

## Componentes
- `auth-server`: servidor OAuth2 Authorization Server.
- `core-bank`: núcleo bancario y procesamiento Batch.
- `bff-web`: BFF para canal web.
- `bff-mobile`: BFF para canal móvil.
- `bff-kiosk`: BFF para kiosco.
- `postgres-banco`: base de datos PostgreSQL.
- `broker`: Apache Kafka.

## 1. Levantar el proyecto
Desde la carpeta raíz `S8`:

```bash
docker compose up --build
```

## 2. OAuth2
El `auth-server` expone el Authorization Server y los BFF funcionan como Resource Server validando JWT.

## 3. Resilience4j
Los tres BFF usan un Circuit Breaker llamado `coreBank` para proteger las llamadas hacia `core-bank`.
Si el Core deja de responder, se ejecuta un fallback y el BFF devuelve una respuesta controlada.

Endpoint de monitoreo en cada BFF:
- `/actuator/health`
- `/actuator/circuitbreakers`
- `/actuator/circuitbreakerevents`

Para demostrar el Circuit Breaker:
1. Levantar todos los servicios.
2. Consumir un endpoint del BFF varias veces.
3. Detener `core-bank`.
4. Repetir la llamada.
5. Verificar la respuesta de fallback y `/actuator/circuitbreakers`.

## 4. Kafka
El `core-bank` publica eventos en el tópico `bank-withdrawals` cada vez que procesa correctamente un retiro.
El `bff-web` consume esos eventos de forma asíncrona.

Endpoint para comprobar eventos consumidos:
- `GET /web/eventos`

## 5. Evidencia recomendada
Guardar capturas de:
1. `docker compose ps` con todos los contenedores activos.
2. Solicitud OAuth2 y token JWT.
3. Endpoint protegido respondiendo correctamente con JWT.
4. `/actuator/circuitbreakers` mostrando `coreBank`.
5. Fallback del BFF con `core-bank` detenido.
6. Retiro procesado por `core-bank`.
7. `GET /web/eventos` mostrando el evento recibido desde Kafka.

## Criterios de la pauta
- OAuth2.0: implementado.
- Imágenes Docker: implementadas mediante Dockerfiles.
- Docker Compose: orquesta aplicaciones, PostgreSQL y Kafka.
- Resilience4j: Circuit Breaker en los BFF.
- Kafka: productor en `core-bank` y consumidor en `bff-web`.
- Entrega: código fuente, documentación y evidencias de ejecución.

## Criterios de seguridad
Seguridad OAuth2 (Obtención de Token)
POST http://localhost:9000/oauth2/token

Authorization: Basic Auth (demo-client / demo-secret)

Body (x-www-form-urlencoded): grant_type = client_credentials

## Para test Postman

Canales Protegidos con JWT (Requieren Header Bearer Token)
Canal Web: GET https://localhost:8081/web/cuentas/105

Canal Móvil: GET https://localhost:8082/mobile/cuentas/105

Canal Kiosco: GET https://localhost:8083/kiosk/cajero/105

Eventos Kafka (Web): GET https://localhost:8081/web/eventos

Monitoreo de Resiliencia (Resilience4j)
GET https://localhost:8081/actuator/health

GET https://localhost:8081/actuator/circuitbreakers