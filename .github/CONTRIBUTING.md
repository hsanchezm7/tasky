# Guía de Contribución a Tasky

¡Gracias por querer colaborar en **Tasky**! Este documento resume los estándares de calidad, las herramientas de desarrollo y el flujo de trabajo que seguimos para mantener el repositorio ordenado y consistente.

## 1. Requisitos del entorno

Antes de empezar, asegúrate de contar con el software necesario:

| Herramienta | Versión requerida | Comprobación |
|-------------|-------------------|--------------|
| **Git**     | Versión reciente  | `git --version` |
| **JDK**     | **21**            | `java -version` |
| **Maven**   | **3.9+**          | `mvn -version`  |

> Asegúrate de que `mvn -version` apunte a Java 21 a través de tu variable de entorno `JAVA_HOME`.

## 2. Configuración inicial del repositorio

Tras clonar el repositorio, es **obligatorio activar los hooks de Git locales** para garantizar el cumplimiento de los estándares de estilo antes de cada commit:

```bash
# 1. Configurar la ruta de hooks versionados
git config core.hooksPath .githooks

# 2. Asignar permisos de ejecución (Linux/macOS)
chmod +x .githooks/pre-commit
```

### ¿Qué hace el hook `pre-commit`?
Ejecuta automáticamente **Spotless** con el formateador `google-java-format` e indexa los ficheros reformateados antes de confirmar el commit.
Si necesitas formatear manualmente en cualquier momento:

```bash
mvn spotless:apply
```

## 3. Uso de Inteligencia Artificial (IA)

El uso de herramientas de asistencia basada en IA (como ChatGPT, GitHub Copilot, Gemini, Claude, etc.) está permitido como apoyo al desarrollo, pero **su uso debe ser transparente y responsable**:

1. **Declaración obligatoria:** Si has utilizado alguna herramienta de IA para generar código, diseñar tests, redactar documentación o resolver incidencias, **debes declararlo explícitamente en la descripción del Pull Request** (indicando la herramienta utilizada y el alcance de su intervención).
2. **Responsabilidad humana:** Quien envía el PR es 100% responsable del código aportado. Debes comprender en profundidad cada línea generada por IA, verificar que no introduzca vulnerabilidades, malas prácticas o dependencias innecesarias, y asegurar que cumple con los estándares del proyecto.
3. **Validación estricta:** El código asistido por IA debe someterse a las mismas pruebas unitarias y revisiones manuales (`curl`, tests aislados) que el código escrito manualmente.

## 4. Flujo de trabajo con Git y GitHub

### 4.1. Reportar problemas o sugerencias (Issues)

Antes de empezar una tarea de gran alcance, abre o busca una Issue existente. Al crear una nueva, utiliza las plantillas disponibles:
* **Bug report** (`.github/ISSUE_TEMPLATE/bug.md`): para fallos de funcionamiento o regresiones.
* **Enhancement** (`.github/ISSUE_TEMPLATE/enhancement.md`): para nuevas características o mejoras técnicas.

### 4.2. Ramas de trabajo

Crea ramas descriptivas a partir de la rama base (`main` o `develop` según corresponda):
* `feat/nombre-funcionalidad`
* `fix/descripcion-del-bug`
* `refactor/nombre-mejora`

### 4.3. Convención de Commits

Seguimos la especificación de [Conventional Commits](https://www.conventionalcommits.org/):

```
tipo(ámbito): descripción clara en imperativo
```

* **Tipos permitidos:** `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `ci`.
* **Ejemplos:**
  * `feat(tasks): añade filtro por estado en GET /api/tasks`
  * `fix(api): devuelve 404 cuando la tarea no existe`
  * `test(service): cubre validacion de fecha limite`
  * `chore(build): añade Spotless para formateo automatico`

## 5. Estándares de desarrollo y pruebas

### 5.1. Arquitectura y Reglas de Negocio

Respeta la separación por capas (`controller`, `service`, `repository`, `dto`, `model`, `exception`):
* Los endpoints REST deben delegar las validaciones estructurales al Bean Validation (`@Valid`) en los DTOs.
* Las reglas de negocio (ej. validación de fechas límite pasadas, inmutabilidad de tareas `COMPLETED`) residen en `TaskServiceImpl`.
* Todo error de negocio o recurso ausente debe canalizarse vía `GlobalExceptionHandler` devolviendo el código HTTP correspondiente (`400`, `404`) en formato JSON.

### 5.2. Pruebas Unitarias

* Las pruebas en `src/test/` deben ser **unitarias puras** (aisladas con Mockito en servicios y validadores en DTOs).
* **No** levantes el contexto completo de Spring ni bases de datos para pruebas que puedan evaluarse de forma unitaria.
* Ejecuta los tests antes de enviar tu trabajo:
  ```bash
  mvn clean test
  ```

## 6. Envío y revisión de Pull Requests (PR)

1. **Abre un Pull Request** completando todos los campos de la plantilla (`.github/PULL_REQUEST_TEMPLATE.md`):
   * Enlace a la Issue correspondiente (`Closes #123`).
   * Resumen del cambio.
   * Lista de comprobación de cómo se ha probado (tests unitarios y/o llamadas `curl`).
   * **Declaración de uso de IA** (especificando si se ha empleado, qué herramienta y en qué partes).
   * Dudas de diseño o notas para quien revise.
2. **Revisión por Code Owners:**
   GitHub solicitará automáticamente la revisión de los propietarios definidos en `.github/CODEOWNERS`.
3. **Criterios de aceptación:**
   * La suite de tests pasa en local y en CI (`mvn clean package`).
   * El código cumple con las reglas de estilo de Spotless (`mvn spotless:check`).
   * Al menos una aprobación formal del equipo sin comentarios pendientes de resolver.
