# Simulador del Examen

Aplicación web para realizar simulacros de exámenes. Los usuarios inician sesión con Google, crean exámenes por tipo, responden preguntas y consultan resultados.

## Tecnologías

- **Java 17** · **Spring Boot 4**
- **Spring Security** · **OAuth2** (Google)
- **Spring Data JPA** · **MySQL**
- **Thymeleaf** · **Bootstrap 5**
- **Apache POI** (importación de preguntas desde Excel)

## Requisitos

- Java 17+
- Maven 3.6+
- MySQL 8+
- Cuenta de Google (para OAuth2)

## Configuración

1. **Base de datos MySQL**

   Crea la base de datos y el usuario:

   ```sql
   CREATE DATABASE simulador CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'usimulador'@'localhost' IDENTIFIED BY 'tu_password';
   GRANT ALL PRIVILEGES ON simulador.* TO 'usimulador'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Credenciales**

   Las credenciales sensibles se configuran mediante variables de entorno o un archivo local:

   - **Opción A (recomendada):** Copia `application-local.properties.example` a `application-local.properties` en la raíz del proyecto y completa los valores. Este archivo está en `.gitignore` y no se sube al repositorio.

   - **Opción B:** Define las variables de entorno `DB_PASSWORD`, `OAUTH_GOOGLE_CLIENT_ID` y `OAUTH_GOOGLE_CLIENT_SECRET` antes de ejecutar.

   Propiedades configurables: `spring.datasource.url` (host, puerto, BD), `spring.datasource.username`, y las credenciales de Google OAuth2.

3. **Google Cloud Console**

   - Crea un proyecto y habilita la API de Google+ / OAuth2.
   - Configura la pantalla de consentimiento.
   - Crea credenciales OAuth 2.0 (tipo “Aplicación web”).
   - Añade `http://localhost:8021/login/oauth2/code/google` como URI de redirección autorizada.

## Ejecución

```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8021`.

## Funcionalidades

| Ruta | Descripción |
|------|-------------|
| `/` | Página de inicio |
| `/examen` | Lista de exámenes del usuario (fecha, tema, aciertos, resultado) |
| `/examen/resultados/{id}` | Detalle de un examen con preguntas, respuestas correctas y feedback |
| `/agregar-examen` | Crear nuevo examen eligiendo tipo y número de preguntas |
| `/examen-en-progreso` | Responder una pregunta del examen |
| `/examen-en-progreso/indice` | Índice de preguntas del examen en curso |
| `/importar-preguntas` | Importar preguntas desde Excel (requiere permiso CARGA_EXCEL) |

### Flujo del examen

1. El usuario crea un examen (tipo y número de preguntas).
2. Responde las preguntas una a una o navega desde el índice.
3. Puede marcar preguntas como pendientes.
4. Al finalizar, se registran `fechaHoraFin` y `minutosTranscurrido`.
5. Se considera **aprobado** con al menos 70% de aciertos.

### Seguridad

- Acceso con Google OAuth2.
- Solo usuarios registrados en `usuario_permiso` pueden usar las rutas protegidas.
- El permiso `CARGA_EXCEL` habilita la importación de preguntas desde Excel.

## Estructura del proyecto

```
src/main/java/mx/lonsung/simulador/
├── config/          # Security, filtros, inicialización
├── controller/      # HomeController, ExamenController, PreguntaImportController
├── entity/          # Examen, Pregunta, ExamenPregunta, UsuarioPermiso, etc.
├── repository/      # JPA repositories
└── service/         # ExamenService, PreguntaImportService, BitacoraService
```
