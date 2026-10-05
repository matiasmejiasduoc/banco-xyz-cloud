# Banco XYZ - Microservicios en la nube

Actividad sumativa semana 8, Desarrollo Backend III.

Continuación del proyecto BFF de la semana 5 (datos de bank_legacy_data). En esta entrega se agregó seguridad con OAuth 2.0, tolerancia a fallos con Resilience4j, mensajería con Kafka y se dockerizó todo para levantarlo con docker compose.

## Estructura

- `auth-server/` servidor de autorización OAuth 2.0 (puerto 9100)
- `commons/` código compartido
- `servicios/` cuentas-service (8081), transacciones-service (8082), movimientos-service (8083)
- `bff/` bff-web (8090), bff-movil (8091), bff-cajero (8092)
- `docker/` script para crear las bases de datos en Postgres
- `evidencia/` salidas de consola y captura de la ejecución

Cada microservicio tiene su `Dockerfile`.

## Lo que se hizo

**OAuth 2.0:** el auth-server entrega tokens JWT. Los BFF y los servicios validan el token y el scope. Cada canal tiene su cliente (portal-web, app-movil, CAJ-001 y CAJ-002) y cada BFF usa client_credentials para llamar a los servicios. El cajero ya no usa los headers X-Cajero-Id / X-Cajero-Clave; el terminal sale del token.

**Resilience4j:** circuit breaker, retry y bulkhead en los clientes de los BFF. Si se cae cuentas-service, el móvil devuelve el último saldo conocido con `referencial: true` y el web responde 503 apenas se abre el circuito.

**Kafka:** cuando se hace un retiro en el cajero se publica un evento en el tópico `banco.retiros`, y movimientos-service lo consume y registra el movimiento. Si el evento viene malo, se manda a `banco.retiros.DLT`. Si Kafka está caído, el retiro se revierte.

**Docker:** el docker-compose levanta Postgres, Kafka, Kafka UI, el auth-server, los 3 servicios y los 3 BFF.

## Ejecutar

Se necesita Docker.

    docker compose up -d --build
    docker compose ps

Para bajar todo:

    docker compose down

Kafka UI queda en http://localhost:8085

## Probar

Primero hay que pedir un token. Por ejemplo, para el web:

    curl -u portal-web:portal-web-secret -d grant_type=client_credentials -d scope=web.read http://localhost:9100/oauth2/token

Y después se usa en los BFF:

    curl -H "Authorization: Bearer <token>" "http://localhost:8090/web/api/panel?desde=2024-01-01&hasta=2024-01-07"

Clientes para probar:

- web: `portal-web` / `portal-web-secret`, scope `web.read`
- móvil: `app-movil` / `app-movil-secret`, scope `movil.read`
- cajero: `CAJ-001` / `caj-001-secret`, scope `cajero.operar`

El retiro en cajero necesita además el header `Idempotency-Key`:

    curl -X POST -H "Authorization: Bearer <token>" -H "Idempotency-Key: retiro-0001" -H "Content-Type: application/json" -d '{"monto":2000}' http://localhost:8092/cajero/api/cuentas/112/retiros

También está el usuario `cliente` / `cliente123` para probar el flujo authorization_code con PKCE.

## Evidencia

En `evidencia/` estan las salidas de consola de cada parte (contenedores, oauth2, bff, kafka, resiliencia y tests) y una captura del docker compose.
