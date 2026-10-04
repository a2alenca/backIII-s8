# Prueba punto a punto - Semana 8

## 1. Docker
```bash
docker compose up --build -d
docker compose ps
```

Deben aparecer: `backendiii-broker`, `postgres-banco-xyz`, `backendiii-auth`, `backendiii-core-bank`, `backendiii-bff-web`, `backendiii-bff-mobile` y `backendiii-bff-kiosk`.

## 2. OAuth2
```bash
curl -s -u demo-client:demo-secret -X POST http://localhost:9000/oauth2/token -H "Content-Type: application/x-www-form-urlencoded" -d "grant_type=client_credentials&scope=catalog.read"
```
Guardar el `access_token`.

Sin token, un endpoint del BFF debe responder `401`.

## 3. Resilience4j
```bash
curl -sk http://localhost:8081/actuator/circuitbreakers
```
Luego detener el Core:
```bash
docker compose stop core-bank
```
Hacer varias llamadas autenticadas al BFF. La llamada debe terminar en el fallback `CORE_BANK_NO_DISPONIBLE` y el estado del Circuit Breaker debe avanzar según la configuración.

Volver a levantar:
```bash
docker compose start core-bank
```

## 4. Kafka
El endpoint de retiro del Core publica en `bank-withdrawals`.

Después de procesar un retiro, consultar con JWT:
```bash
curl -sk -H "Authorization: Bearer <TOKEN>" https://localhost:8081/web/eventos
```
Debe aparecer el JSON del evento `RETIRO_PROCESADO` consumido por `bff-web`.

## 5. Evidencias
Tomar capturas de:
- `docker compose ps`
- token OAuth2
- `401` sin token
- respuesta autorizada con JWT
- `/actuator/circuitbreakers`
- fallback con Core detenido
- retiro procesado
- `/web/eventos` mostrando el evento Kafka
