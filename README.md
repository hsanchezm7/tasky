# tasky

API REST de gestión de tareas (To-Do) construida con **Spring Boot 3**, **Java 21**, **Spring Data JPA / Hibernate** y **PostgreSQL**. Es la base del proyecto que se irá ampliando durante el curso (Docker, CI/CD y despliegue).

Cada tarea tiene título, descripción, estado, prioridad y fecha límite. La API ofrece un CRUD completo, filtrado por estado, validaciones con Bean Validation y manejo de errores centralizado (un recurso inexistente devuelve `404` con un JSON de error, nunca una traza).

## Requisitos

| Herramienta | Versión | Comprobación |
|-------------|---------|--------------|
| Git         | cualquiera reciente | `git --version` |
| JDK         | **21**  | `java -version` |
| Maven       | 3.9+    | `mvn -version` |
| Docker (con Compose) | cualquiera reciente | `docker compose version` |

`mvn -version` debe mostrar también Java 21. Si muestra otra versión, apunta `JAVA_HOME` a un JDK 21.

La aplicación usa **PostgreSQL**. No hace falta instalarlo: el repositorio incluye un `docker-compose.yml` que lo levanta en un contenedor. Si prefieres usar un PostgreSQL propio, también sirve (ver [Configuración de la base de datos](#configuración-de-la-base-de-datos)).

## Puesta en marcha desde cero

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio> tasky
cd tasky
```

### 2. Activar los hooks de Git

El repositorio versiona sus hooks en `.githooks/`. Git no los usa por defecto (busca en `.git/hooks/`, que no se versiona), así que **cada persona que clone el repositorio debe ejecutar esto una vez**:

```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit   # solo si el hook no fuera ejecutable
```

El hook `pre-commit` ejecuta Spotless antes de cada commit para que el código se formatee solo:

```sh
#!/bin/sh
echo "Formatting with Spotless..."
mvn -q spotless:apply || exit 1
git add -u          # re-añade los archivos que Spotless haya reformateado
```

> Un hook local se puede saltar con `git commit --no-verify`, así que no sustituye a la comprobación en CI. Además, el hook necesita `mvn` en el `PATH` de la terminal desde la que se hace el commit.

### 3. Construir

```bash
mvn clean package
```

Este comando compila, ejecuta los tests unitarios y genera un ejecutable en `target/tasky.jar`, con todas las dependencias y un servidor web embebido.

### 4. Crear el fichero `.env`

```bash
cp .env.example .env
```

`docker-compose.yml` no guarda la contraseña: la exige con `${POSTGRES_PASSWORD:?...}`, así que **Compose se niega a arrancar si falta el `.env`** (o la variable). El `.env` está en `.gitignore`; edítalo para poner tus propios valores.

| Variable | Valor en `.env.example` | Descripción |
|----------|-------------------------|-------------|
| `API_PORT`          | `8084`             | Puerto del host donde se publica la API |
| `POSTGRES_DB`       | `tareas`           | Base de datos |
| `POSTGRES_USER`     | `app`              | Usuario |
| `POSTGRES_PASSWORD` | `secret_local_dev` | Contraseña (obligatoria) |

### 5. Levantar la aplicación con Docker Compose

```bash
docker compose up -d --build
docker compose ps        # espera a que db y api aparezcan como (healthy) / running
```

Se levantan dos servicios:

- **`db`**: PostgreSQL 16. Dentro de la red de Compose escucha en `db:5432`; además se publica en `127.0.0.1:5432` solo para conectarse con un cliente SQL desde tu máquina. Los datos se guardan en el volumen `db-data`, así que **persisten entre reinicios**.
- **`api`**: la aplicación, construida con el `Dockerfile`. Arranca cuando `db` está *healthy* y se publica en **`http://localhost:8084`** (configurable con `API_PORT`).

El healthcheck de `db` usa `pg_isready -U $${POSTGRES_USER} -d $${POSTGRES_DB}`. El `$$` escapa la interpolación de Compose: sin él, Compose sustituiría la variable al leer el YAML; con él, llega un `$` literal y la expande el shell **dentro del contenedor**, donde esas variables ya existen.

Para pararlo, `docker compose down` (añade `-v` para borrar también los datos). Hibernate crea y actualiza las tablas automáticamente al arrancar (`spring.jpa.hibernate.ddl-auto=update`).

> **Sin Docker para la API.** También puedes levantar solo la base de datos (`docker compose up -d db`) y ejecutar la API en local con `java -jar target/tasky.jar`, apuntándola a ella con las variables de [Configuración de la base de datos](#configuración-de-la-base-de-datos). En ese caso la API escucha en `http://localhost:8080`.

### 6. Comprobar que funciona

Se registra una tarea y se recupera (si ejecutas la API con `java -jar`, cambia el puerto a `8080`):

```bash
# Crear una tarea (201 Created; el estado por defecto es PENDING)
curl -i -X POST http://localhost:8084/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Entregar práctica",
        "description": "Práctica de Spring Boot",
        "priority": "HIGH",
        "dueDate": "2026-12-31"
      }'

# Recuperarla por id (200 OK)
curl -i http://localhost:8084/api/tasks/1

# Listar todas, o filtrar por estado
curl -i http://localhost:8084/api/tasks
curl -i "http://localhost:8084/api/tasks?status=PENDING"

# Resumen de tareas
curl -i http://localhost:8084/api/tasks/stats

# Estado de la aplicación (Actuator)
curl -i http://localhost:8084/actuator/health

# Recurso inexistente: 404 con JSON de error
curl -i http://localhost:8084/api/tasks/999
```

La fecha límite debe ser hoy o futura. Para inspeccionar los datos directamente en la base de datos:

```bash
docker compose exec db psql -U app -d tareas -c "SELECT * FROM tasks;"
```

### Configuración de la base de datos

Dentro de Docker Compose, la API recibe la conexión en `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`, construidas a partir del `.env`. No hay que tocar nada.

Si ejecutas la API fuera de Docker (`java -jar`), la conexión se configura con estas variables de entorno:

| Variable | Por defecto | Descripción |
|----------|-------------|-------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/tasky` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | `tasky` | Usuario |
| `DB_PASSWORD` | `tasky` | Contraseña |

Los valores por defecto **no** coinciden con la base de datos de Compose, así que para usarla hay que indicarlos:

```bash
DB_URL=jdbc:postgresql://localhost:5432/tareas DB_USERNAME=app DB_PASSWORD=secret_local_dev java -jar target/tasky.jar
```

## Dev Container

El repositorio incluye `.devcontainer/devcontainer.json` para trabajar en un entorno ya preparado, sin instalar nada salvo Docker y VS Code (con la extensión *Dev Containers*) o GitHub Codespaces.

- **Imagen**: `mcr.microsoft.com/devcontainers/java:21`, con Maven (feature `java`) y Docker-in-Docker (feature `docker-in-docker`), así que dentro del contenedor funcionan `mvn` y `docker compose`.
- **Puertos reenviados**: `8080` (API) y `5432` (PostgreSQL).
- **`postCreateCommand`**: crea el `.env` a partir de `.env.example` si no existe, activa los hooks (`git config core.hooksPath .githooks`) y descarga las dependencias con `mvn -B dependency:go-offline`.
- **Extensiones**: Java Extension Pack y Docker.

Para usarlo: abre el repositorio en VS Code y elige **Dev Containers: Reopen in Container**. La primera vez tarda unos minutos; después ya puedes ejecutar `mvn clean package` o `docker compose up -d --build` desde la terminal integrada. `devcontainer-lock.json` fija las versiones exactas de las features para que todos usen el mismo entorno.

## API

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST   | `/api/tasks`      | Crea una tarea |
| GET    | `/api/tasks`      | Lista las tareas; admite `?status=` para filtrar por estado |
| GET    | `/api/tasks/stats` | Resumen: total, recuento por estado y por prioridad, y tareas vencidas |
| GET    | `/api/tasks/{id}` | Obtiene una tarea por id |
| PUT    | `/api/tasks/{id}` | Actualiza una tarea existente |
| DELETE | `/api/tasks/{id}` | Elimina una tarea |

Códigos de respuesta: `200` OK, `201` creada, `204` borrada, `400` petición inválida (validación, regla de negocio o valor de `status` no válido) y `404` tarea inexistente.

### Modelo `Task`

| Campo | Tipo | Reglas |
|-------|------|--------|
| `id`          | `Long`         | Autogenerado |
| `title`       | `String`       | Obligatorio, máx. 100 caracteres |
| `description` | `String`       | Opcional, máx. 1000 caracteres |
| `status`      | `TaskStatus`   | `PENDING`, `IN_PROGRESS`, `COMPLETED` o `SUSPENDED`; `PENDING` por defecto al crear |
| `priority`    | `TaskPriority` | Obligatorio: `LOW`, `MEDIUM` o `HIGH` |
| `dueDate`     | `LocalDate`    | Obligatorio, no puede ser una fecha pasada |

### Reglas de negocio

1. No se puede crear ni actualizar una tarea con fecha límite pasada.
2. Si no se indica estado al crear, se asigna `PENDING`.
3. Una tarea `COMPLETED` no puede volver a modificarse.
4. El listado puede filtrarse por estado con `?status=`; sin filtro devuelve todas las tareas.
5. Leer, actualizar o borrar un `id` inexistente devuelve `404`.
6. Una tarea está **vencida** si su fecha límite es anterior a hoy y su estado no es `COMPLETED` ni `SUSPENDED`; la que vence hoy no cuenta.

### Resumen de tareas

`GET /api/tasks/stats` devuelve un resumen agregado. Los recuentos incluyen todos los estados y prioridades, también los que tienen 0 tareas:

```json
{
  "total": 6,
  "byStatus":   { "PENDING": 3, "IN_PROGRESS": 1, "COMPLETED": 2, "SUSPENDED": 0 },
  "byPriority": { "LOW": 1, "MEDIUM": 4, "HIGH": 1 },
  "overdue": 2
}
```

## Estructura del proyecto

```
.githooks/pre-commit                       # Hook: formatea con Spotless antes de cada commit
.devcontainer/                             # Entorno de desarrollo en contenedor
Dockerfile, docker-compose.yml, .env.example  # Imagen de la API y orquestación con PostgreSQL
src/main/java/es/um/pc/tasky
├── TaskyApplication.java                  # Clase principal
├── model/                                 # Entidad Task y enums TaskStatus / TaskPriority
├── repository/                            # TaskRepository (Spring Data JPA)
├── dto/                                   # TaskRequest (con validaciones), TaskResponse y TaskStatsResponse
├── service/                               # TaskService y TaskServiceImpl (reglas de negocio)
├── controller/                            # TaskController (endpoints REST)
└── exception/                             # Excepciones de negocio y GlobalExceptionHandler
src/test/java/es/um/pc/tasky
├── service/TaskServiceImplTest.java       # Reglas de negocio, con Mockito
└── dto/TaskRequestValidationTest.java     # Bean Validation
```

Los tests son **unitarios puros**: no levantan el contexto de Spring ni una base de datos.

## Convenciones

**Commits.** Se usa [Conventional Commits](https://www.conventionalcommits.org/): `tipo(ámbito): descripción en imperativo`.

```
feat(tasks): añade filtro por estado en GET /api/tasks
fix(api): devuelve 404 cuando la tarea no existe
test(service): cubre la validación de fecha límite
chore(build): añade Spotless para formateo automático
docs(readme): documenta la puesta en marcha
```

Tipos habituales: `feat`, `fix`, `docs`, `test`, `refactor`, `build`, `chore` y `ci`.

**Formato.** El código Java sigue el estilo de Google (`google-java-format`) aplicado con [Spotless](https://github.com/diffplug/spotless). El hook de pre-commit lo aplica automáticamente, y también se puede ejecutar a mano con `mvn spotless:apply`.

