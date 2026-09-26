# Plaza Órbita — App móvil (Kotlin) + Backend (Spring Boot/Kotlin) + MySQL

## ⚠️ Nota importante sobre la arquitectura

El acta de constitución que subiste describe Plaza Órbita como una **plataforma web**
("paneles web", API REST, base de datos relacional). Tú pediste construirlo como
**app móvil nativa en Kotlin**, así que este proyecto respeta esa decisión pero
mantiene la arquitectura de 3 capas del acta:

```
[ App Android (Kotlin + Jetpack Compose) ]
              |  HTTP/JSON (Retrofit)
              v
[ Backend REST API (Spring Boot + Kotlin) ]
              |  JDBC
              v
[ MySQL ]
```

**Por qué hay un backend en medio y la app no se conecta directo a MySQL:**
Un Android NO debe conectarse directamente a una base de datos MySQL. Requeriría
que la contraseña de la base de datos viajara dentro del APK (cualquiera puede
descompilarlo y leerla) y expondría el puerto 3306 de tu base de datos a
internet. El RNF-03 del acta ("HTTPS/TLS, hash de contraseñas, RBAC") solo es
alcanzable con una API intermedia. Esta es la forma estándar en la industria
de conectar apps móviles a bases de datos relacionales.

Como todo el stack (app + backend) está en Kotlin, aprendes el lenguaje en
ambos lados sin cambiar de sintaxis.

## Estructura de carpetas

```
PlazaOrbita/
├── database/
│   └── schema.sql              # Esquema completo de MySQL (6 tablas, todos los módulos)
├── backend/                    # API REST en Spring Boot + Kotlin
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/plazaorbita/backend/
│       ├── model/              # Entidades JPA (User, Business, Product, Appointment, Order, Notification)
│       ├── repository/         # Interfaces Spring Data JPA
│       ├── dto/                # Objetos de petición/respuesta
│       ├── security/           # JWT (generación y validación de tokens)
│       ├── config/             # Configuración de Spring Security
│       ├── service/            # Lógica de negocio de cada módulo
│       └── controller/         # Endpoints REST (/api/auth, /api/businesses, ...)
└── android-app/                # App nativa en Kotlin + Jetpack Compose
    └── app/src/main/java/com/plazaorbita/app/
        ├── data/model/         # Data classes (equivalentes a los DTOs del backend)
        ├── data/remote/        # Retrofit (ApiService + cliente HTTP)
        ├── ui/auth/            # Login y Registro (funcionales, conectados al backend)
        ├── ui/admin/           # Panel del administrador de la plaza
        ├── ui/business/        # Panel del dueño de negocio
        ├── ui/customer/        # Panel del cliente
        ├── ui/navigation/      # Navegación (redirige según el rol tras login)
        └── util/SessionManager.kt  # Guarda el token JWT en el dispositivo
```

## Qué está 100% funcional ahora mismo

- **Registro y login** (app ↔ backend ↔ MySQL) con contraseñas hasheadas (BCrypt) y JWT.
- **Redirección automática por rol** tras iniciar sesión (admin / dueño / cliente).
- **Negocios**: crear y listar (`/api/businesses`).
- **Inventario**: crear productos, listar, y bandera automática de "stock bajo" (Historia 1).
- **Citas**: reservar con bloqueo de horario duplicado a nivel de base de datos y de código (Historia 2).
- **Pedidos**: crear pedido con descuento de stock, cambio de estatus, notificación al cliente cuando está listo (Historia 3 y 4).
- **Notificaciones**: se generan automáticamente al crear cita/pedido; endpoint para listarlas.
- **Reportes**: conteo de pedidos/citas por negocio (Historia 5), listo para ampliarse.

Las pantallas de Admin/Dueño/Cliente ya cargan datos reales del backend; los
formularios de "crear cita", "crear pedido" y "dar de alta negocio" quedan
marcados con `// TODO siguiente sprint` en el código — siguen exactamente el
mismo patrón que Login/Register, así que decirme "hazme la pantalla de X" te
la genero igual de completa.

## Cómo correrlo

### 1. Base de datos
```bash
mysql -u root -p < database/schema.sql
```

### 2. Backend
1. Abre la carpeta `backend/` en IntelliJ IDEA o Android Studio (con plugin de Kotlin).
2. Edita `src/main/resources/application.properties`: pon tu password real de MySQL
   y cambia `app.jwt.secret` por una cadena larga y aleatoria.
3. Ejecuta `PlazaOrbitaApplication.kt` (o `./gradlew bootRun`).
4. Debe quedar escuchando en `http://localhost:8080`.

### 3. App Android
1. Abre la carpeta `android-app/` en **Android Studio**.
2. Deja que Gradle sincronice (puede tardar la primera vez).
3. Corre la app en el **emulador** — ya está configurada para hablar con
   `http://10.0.2.2:8080/`, que es como el emulador ve el `localhost` de tu PC.
   Si usas un celular físico conectado por USB, cambia esa URL en
   `RetrofitClient.kt` por la IP local de tu computadora (ej. `http://192.168.1.50:8080/`).
4. Prueba: Registrarme → elige un rol → deberías caer en el panel correspondiente.

## Próximos pasos sugeridos (Sprint 2 en adelante, según tu cronograma)

- Formulario de alta de producto con validación de umbral mínimo.
- Pantalla de reserva de cita con selector de fecha/hora (`DatePicker`/`TimePicker` de Compose).
- Pantalla de carrito de compra para pedidos con pickup.
- Pantalla de notificaciones (ya hay endpoint listo: `/api/notifications/user/{id}`).
- Restringir cada endpoint por rol en `SecurityConfig.kt` (ahora solo exige estar autenticado; falta `hasRole(...)` por módulo).
