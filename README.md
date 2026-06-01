# 📦 ms-inventario | Sistema de Gestión y Arriendo de Implementos Deportivos

Este microservicio actúa como el **módulo de control de inventario y arriendos de implementos** dentro del ecosistema municipal. Su objetivo es gestionar el stock físico de artículos deportivos asociados a los recintos, procesar las solicitudes de asignación/arriendo síncronas provenientes de otros microservicios (como `ms-reservas`) calculando cobros extra, y liberar el material al finalizar los bloques horarios.

---

## 🛠️ Stack Tecnológico Core
* **Lenguaje:** Java 21 LTS
* **Framework:** Spring Boot 4.0.6 (Spring Security & Method Security habilitados)
* **Gestor de Dependencias:** Maven
* **Persistencia:** Spring Data JPA / Hibernate
* **Base de Datos:** MySQL (Puerto de red 3317 gestionado en contenedor local)
* **Seguridad:** Spring Security, Filtro de extracción de Claims JWT (Roles: ADMIN, USER)

---

## 📂 Árbol de Directorios Real
```text
ms-inventario/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── cl/
│   │   │       └── municipalidad/
│   │   │           └── inventario/
│   │   │               ├── controller/
│   │   │               │   └── ItemInventarioController.java
│   │   │               ├── dto/
│   │   │               │   ├── request/
│   │   │               │   │   └── SolicitudArriendoDTO.java
│   │   │               │   └── response/
│   │   │               │       └── ResultadoArriendoDTO.java
│   │   │               ├── entity/
│   │   │               │   └── ItemInventario.java
│   │   │               ├── exception/
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   └── StockInsuficienteException.java
│   │   │               ├── repository/
│   │   │               │   └── ItemInventarioRepository.java
│   │   │               ├── security/
│   │   │               │   ├── JwtAuthenticationFilter.java
│   │   │               │   └── SecurityConfig.java
│   │   │               └── InventarioApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
├── compose.yml
└── pom.xml

```
## 🚀 Integración y Comunicación del Sistema

### 1. Obtener Inventario Disponible por Recinto
Filtra y lista todos los artículos deportivos que pertenecen a un recinto específico.

* **Método:** `GET`
* **URL:** `http://localhost:8081/api/v1/inventario/recinto/{idRecinto}`
* **Seguridad:** 🔒 Requiere Token JWT válido en la cabecera.

---

### 2. Crear o Actualizar Item en Inventario
Agrega nuevos artículos (ej: Balones, chalecos) o actualiza precios y stock base.

* **Método:** `POST`
* **URL:** `http://localhost:8081/api/v1/inventario`
* **Seguridad:** 🛡️ Restringido exclusivamente a usuarios con rol ADMIN (`@PreAuthorize("hasRole('ADMIN')")`).

**📥 JSON Request:**
```json
{
    "nombre": "Balón de Fútbol N°5",
    "idRecintoForaneo": 1,
    "stockTotal": 20,
    "stockDisponible": 20,
    "precioArriendo": 1500.0
}
```
### 3. Asignar / Alquilar Inventario (Consumido por ms-reservas)
Endpoint síncrono que valida el stock de múltiples artículos en bloque. Si hay disponibilidad, descuenta del stock disponible y calcula el costo extra acumulado.

URL: POST http://localhost:8081/api/v1/inventario/asignar

Seguridad: 🔓 Acceso liberado de forma interna (permitAll()), validado por lógica de negocio.

📥 JSON Request:

```
[
    {
        "idItem": 1,
        "cantidad": 2
    },
    {
        "idItem": 2,
        "cantidad": 5
    }
]
```
📤 JSON Response (Éxito - HTTP 200 OK):

```
{
    "exito": true,
    "costoTotalExtra": 3000.0,
    "mensaje": "Reserva de inventario exitosa."
}
```
⚠️ JSON Response (Fallo por Stock - HTTP 404/400 gestionado por GlobalException):

```
{
    "timestamp": "2026-05-31T21:15:00",
    "status": 404,
    "error": "Not Found",
    "message": "Stock insuficiente para: Balón de Fútbol N°5. Disponible: 1, Solicitado: 2"
}
```
### 4. Liberar Inventario
Devuelve la cantidad de artículos prestados al stock disponible una vez concluido el bloque de juego o si se cancela la reserva.

URL: POST http://localhost:8081/api/v1/inventario/liberar

Seguridad: 🔓 Acceso liberado de forma interna (permitAll()).

📥 JSON Request:
```
[
    {
        "idItem": 1,
        "cantidad": 2
    }
]
```
### 🐳 Infraestructura Local (Docker)
El servicio autogestiona su persistencia local. Para levantar la base de datos aislada y su interfaz gráfica ejecutando el archivo compose.yml:


⚙️ Comando de ejecución:
```bash
docker compose up -d
```

🗄️ MySQL: Puerto 3317 (Mapeado a base de datos interna db_inventario).

📊 phpMyAdmin: Accesible localmente en http://localhost:9017.
