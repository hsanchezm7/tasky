# Boletín 1

## Resumen de commits

| # | Mensaje del commit | Qué se hizo | Autor | Enlace |
|---|--------------------|-------------|-------|--------|
| 1 | `chore: proyecto Maven inicial de la API de tareas` | Creación del proyecto Spring Boot 3 con Java 21, entidad `Task`, arquitectura por capas (controlador, servicio, repositorio), manejo global de excepciones, tests unitarios, `.gitignore` y `.gitattributes`. | Hugo Sánchez | [9e306f5](https://github.com/hsanchezm7/tasky/commit/9e306f5420e8f2f933d2ebaba4beef172702a3da) |
| 2 | `chore(editor): add .editorconfig` | Adición del archivo `.editorconfig` para estandarizar la codificación (UTF-8), finales de línea (LF) e indentación del proyecto. | Hugo Sánchez | [c455a1d](https://github.com/hsanchezm7/tasky/commit/c455a1dccd1d9c471a07ba32195720b52f6f0bc6) |
| 3 | `feat(tasks): añadir filtro por estado en GET /api/tasks` | Implementación de filtrado por estado (`TaskStatus`) en el endpoint `GET /api/tasks`, consulta personalizada en repositorio, manejo de errores de conversión y tests unitarios. | Hugo Sánchez | [fbdf7c1](https://github.com/hsanchezm7/tasky/commit/fbdf7c145bf6ebe021a086192bdeb0bb22f889c8) |
| 4 | `chore(build): añade Spotless para formateo automático` | Integración del plugin `spotless-maven-plugin` con Google Java Format, creación del hook `.githooks/pre-commit` y formateo de todo el código base. | Hugo Sánchez | [0eb594a](https://github.com/hsanchezm7/tasky/commit/0eb594aca86a4ba37696bc3e48a16a414fea60db) |

## Detalle por commit

### 1. `chore: proyecto Maven inicial de la API de tareas`

- **Qué se hizo:**

  - Configuración inicial de Maven (`pom.xml`) con Spring Boot 3.3.4, Java 21, dependencias de Spring Web, Spring Data JPA, H2 Database, Bean Validation y Spring Boot Starter Test (JUnit 5 + Mockito).

  - Configuración de `application.properties` con base de datos en memoria H2 (`jdbc:h2:mem:taskydb`).

  - Creación de la entidad de dominio `Task` con campos: `id`, `title`, `description`, `status` (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), `priority` (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), `dueDate`, `createdAt` y `updatedAt`.

  - Implementación del DTO `TaskRequest` con validaciones declarativas (`@NotBlank`, `@Size`, `@NotNull`, `@FutureOrPresent`) y `TaskResponse`.

  - Arquitectura en tres capas:

    - **Repositorio:** `TaskRepository` extendiendo `JpaRepository`.

    - **Servicio:** `TaskService` y `TaskServiceImpl` con lógica de negocio para CRUD completo, validaciones adicionales y control de fechas.

    - **Controlador:** `TaskController` exponiendo endpoints REST (`POST`, `GET`, `PUT`, `DELETE`).

  - Manejo global de excepciones con `@RestControllerAdvice` (`GlobalExceptionHandler`) devolviendo respuestas estructuradas `ErrorResponse` (404 para recursos inexistentes, 400 para validaciones fallidas y errores de negocio).

  - Tests unitarios estrictos (sin levantar contexto Spring ni base de datos): `TaskRequestValidationTest` y `TaskServiceImplTest` cubriendo reglas de negocio y casos límite.

  - Creación de archivos `.gitignore` y `.gitattributes` para normalización de saltos de línea (LF).

- **Autor:** Hugo Sánchez (`hugosanchezmartinez4@gmail.com`)

- **Relación con el boletín:** Apartados **Parte B** (Generar la aplicación con ayuda de IA), **Parte C** (Construir y probar en local) y **Parte D** (Control de versiones con Git - commit inicial).


- **Uso de IA:**

  - **Herramienta:** Asistente de IA (Gemini / Claude).

  - **Prompt utilizado:**

    > *"Genera un proyecto Maven con Spring Boot 3 y Java 21: una API REST de gestión de tareas (entidad Task con id, título, descripción, estado, prioridad y fecha límite). Usa Spring Data JPA con Hibernate para la persistencia y H2 en memoria como base de datos. Incluye endpoints CRUD, capa de servicio, repositorio JPA, validaciones con Bean Validation, manejo de errores con @RestControllerAdvice y tests unitarios con JUnit 5 y Mockito sin arrancar el contexto de Spring."*

### 2. `chore(editor): add .editorconfig`

- **Qué se hizo:**

  - Creación del archivo de configuración `.editorconfig` en la raíz del repositorio para garantizar consistencia entre distintos entornos de desarrollo y editores de código (formato UTF-8, saltos de línea LF, inserción de nueva línea al final del archivo, indentación de 4 espacios para código Java y 2 espacios para YAML/JSON).

- **Autor:** Hugo Sánchez (`hugosanchezmartinez4@gmail.com`)

- **Relación con el boletín:** Apartado **Parte D** (Control de versiones con Git - configuración de entorno y commit `chore` separado).

- **Uso de IA:** No se ha usado (configuración estándar establecida según las especificaciones de la práctica).

### 3. `feat(tasks): añadir filtro por estado en GET /api/tasks`

- **Qué se hizo:**

  - Creación de una rama de funcionalidad (`feat/filtro-estado` o equivalente) para desarrollar un cambio no trivial de forma aislada.

  - Modificación de `TaskRepository` añadiendo el método de consulta `List<Task> findByStatus(TaskStatus status)`.

  - Ampliación de `TaskService` y `TaskServiceImpl` para soportar la obtención de tareas filtradas por estado o el listado general en caso de no indicar parámetro.

  - Actualización de `TaskController` para aceptar el parámetro opcional `GET /api/tasks?status={PENDING|IN_PROGRESS|COMPLETED|CANCELLED}`.

  - Ampliación del manejador global de excepciones para gestionar errores de conversión de parámetros de consulta (`MethodArgumentTypeMismatchException`).

  - Adición de tests unitarios específicos en `TaskServiceImplTest` para comprobar el filtrado por estado y casos sin resultados.

  - Fusión de la rama en `main` mediante `git merge`.

- **Autor:** Hugo Sánchez (`hugosanchezmartinez4@gmail.com`)

- **Relación con el boletín:** Apartado **Parte D** (Control de versiones con Git - flujo de ramas, commits convencionales y merge en `main`).

- **Uso de IA:**

  - **Herramienta:** Asistente de IA.

  - **Prompt utilizado:**

    > *"Añade soporte para filtrar tareas por su estado (enum TaskStatus) en el endpoint GET /api/tasks como parámetro opcional. Actualiza el repositorio JPA, la capa de servicio, el controlador y añade los tests unitarios correspondientes en TaskServiceImplTest."*

### 4. `chore(build): añade Spotless para formateo automático`

- **Qué se hizo:**

  - Integración del plugin `spotless-maven-plugin` (versión `2.43.0`) en el archivo `pom.xml`, configurado con `googleJavaFormat`, eliminación de imports no usados, recorte de espacios en blanco y salto de línea final.

  - Creación del script ejecutable `.githooks/pre-commit` que invoca automáticamente `mvn -q spotless:apply` y re-añade los archivos modificados con `git add -u` antes de confirmar cada commit.

  - Configuración de la ruta de hooks del repositorio mediante `git config core.hooksPath .githooks`.

  - Ejecución de `mvn spotless:apply` para reformatear todo el código fuente del proyecto según el estándar definido.

- **Autor:** Hugo Sánchez (`hugosanchezmartinez4@gmail.com`)

- **Relación con el boletín:** Apartado **Parte E** (Formateo automático en cada commit con Spotless y hooks de Git).

- **Uso de IA:** No se ha usado (implementación directa siguiendo la guía del boletín).

## Descripción narrativa de lo hecho en la sesión y relación con los commits

### Parte A — Instalación y verificación del entorno

En esta primera fase se verificó la disponibilidad de las herramientas fundamentales en el sistema ejecutando:

- `git --version` (comprobando la versión instalada de Git).

- `java -version` (verificando la presencia de OpenJDK 21).

- `mvn -version` (verificando Apache Maven).

Posteriormente se estableció la configuración global de Git para el usuario, definiendo el nombre y correo institucional, la política de pull segura (`pull.ff only` para prevenir merges accidentales no lineales) y la rama principal por defecto:

```bash
git config --global user.name "Hugo Sánchez"
git config --global user.email "hugosanchezmartinez4@gmail.com"
git config --global pull.ff only
git config --global init.defaultBranch main
```

### Parte B — Generar la aplicación con ayuda de IA

Se diseñó y generó la API REST **Tasky** con Spring Boot 3 y Java 21 apoyándose en modelos de IA para generar la base del proyecto con una arquitectura limpia y desacoplada:

1. **Dominio:** Entidad `Task` que modela tareas con identificador, título, descripción, estado (`TaskStatus`), prioridad (`TaskPriority`), fecha de vencimiento (`dueDate`) y marcas de tiempo de auditoría (`createdAt`, `updatedAt`).

2. **Validaciones:** Uso de Jakarta Bean Validation en los DTOs para asegurar que los títulos no estén vacíos, las fechas de vencimiento sean futuras o presentes, y las prioridades sean válidas.

3. **Persistencia:** Base de datos en memoria H2 con Spring Data JPA.

4. **Manejo de Errores:** Implementación de `@RestControllerAdvice` con `GlobalExceptionHandler` para interceptar excepciones como `ResourceNotFoundException`, validaciones `@Valid` (`MethodArgumentNotValidException`) y errores de lógica de negocio, devolviendo siempre un payload JSON `ErrorResponse` coherente con código HTTP adecuado (404, 400).

5. **Tests Unitarios:** Creación de una suite de tests con JUnit 5 y Mockito que valida las reglas de negocio sin levantar el contexto de Spring (manteniendo la ejecución rápida y puramente unitaria).

### Parte C — Construir y probar en local

Se compiló y empaquetó la aplicación generando el fat JAR ejecutable:

```bash
mvn clean package
```

El build generó satisfactoriamente `target/tasky.jar`. Se arrancó la aplicación con:

```bash
java -jar target/tasky.jar
```

Y se realizaron pruebas funcionales de los endpoints mediante `curl` en una terminal secundaria para verificar el correcto funcionamiento del CRUD:

- Creación de tareas (`POST /api/tasks`)

- Listado de tareas (`GET /api/tasks`)

- Consulta por ID (`GET /api/tasks/{id}`)

- Actualización de estado y datos (`PUT /api/tasks/{id}`)

- Eliminación de tareas (`DELETE /api/tasks/{id}`)

- Comprobación de respuestas de error 404 y 400.

### Parte D — Control de versiones con Git

Se inicializó el repositorio local con `git init`. A continuación:

1. Se crearon los archivos `.gitignore` (para excluir `target/`, `.class`, `.idea/`, etc.) y `.gitattributes` (para fijar `eol=lf` y tratar ficheros binarios adecuadamente).

2. Se realizó el commit inicial con tipo convencional:

   - **Commit 1:** `chore: proyecto Maven inicial de la API de tareas` ([9e306f5](https://github.com/hsanchezm7/tasky/commit/9e306f5420e8f2f933d2ebaba4beef172702a3da)).

3. Se añadió el archivo `.editorconfig` y se registró en un commit independiente:

   - **Commit 2:** `chore(editor): add .editorconfig` ([c455a1d](https://github.com/hsanchezm7/tasky/commit/c455a1dccd1d9c471a07ba32195720b52f6f0bc6)).

4. Se creó una rama de trabajo para desarrollar una nueva característica (filtro de tareas por estado en `GET /api/tasks?status=...`) junto con sus tests unitarios asociados. Tras verificar los tests, se realizó el commit en la rama:

   - **Commit 3:** `feat(tasks): añadir filtro por estado en GET /api/tasks` ([fbdf7c1](https://github.com/hsanchezm7/tasky/commit/fbdf7c145bf6ebe021a086192bdeb0bb22f889c8)).

5. Se cambió de nuevo a la rama principal `main` y se fusionó la rama de la característica con `git merge`.

### Parte E — Formateo automático en cada commit

Para garantizar un estilo de código homogéneo en el repositorio:

1. Se incorporó el plugin `spotless-maven-plugin` (versión 2.43.0) en el `pom.xml`, configurado con Google Java Format.

2. Se comprobaron los comandos `mvn spotless:check` y `mvn spotless:apply`.

3. Se creó el hook de Git versionado en `.githooks/pre-commit` con permisos de ejecución:

   ```sh
   #!/bin/sh
   echo "Formatting with Spotless..."
   mvn -q spotless:apply || exit 1
   git add -u
   ```

4. Se configuró Git para utilizar el directorio `.githooks` mediante `git config core.hooksPath .githooks`.

5. Se aplicó el formateo a todo el proyecto y se guardaron los cambios del plugin y hook:

   - **Commit 4:** `chore(build): añade Spotless para formateo automático` ([0eb594a](https://github.com/hsanchezm7/tasky/commit/0eb594aca86a4ba37696bc3e48a16a414fea60db)).
