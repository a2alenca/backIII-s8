#!/usr/bin/env bash
set -euo pipefail

echo '=== Backend III - pruebas rápidas ==='
echo '1) Estado Docker:'
docker compose ps

echo
 echo '2) Obtener token OAuth2:'
curl -s -u demo-client:demo-secret -X POST http://localhost:9000/oauth2/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials&scope=catalog.read' || true

echo
 echo '3) Circuit Breaker BFF Web:'
curl -sk http://localhost:8081/actuator/circuitbreakers || true

echo
 echo '4) Eventos Kafka consumidos por BFF Web (requiere JWT):'
echo 'curl -sk -H "Authorization: Bearer <TOKEN>" https://localhost:8081/web/eventos'
echo
 echo 'Para probar el fallback: detener core-bank y repetir una llamada autenticada al BFF.'
