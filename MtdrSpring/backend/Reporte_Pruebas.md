# Reporte de Pruebas - Sistema de Gestión de Tareas con Telegram Bot

## 1. Introducción

Este reporte documenta las pruebas realizadas en el sistema de gestión de tareas "MyTodoList", una aplicación que integra un frontend en React, un backend en Spring Boot y un bot de Telegram. Las pruebas verifican la funcionalidad, rendimiento y fiabilidad del sistema.

## 2. Estrategia de Pruebas

### Tipos de Pruebas Implementadas

- **Pruebas Unitarias**: Verifican componentes individuales tanto en frontend como backend.
- **Pruebas de Integración**: Comprueban la interacción entre componentes, especialmente entre el backend y el bot de Telegram.
- **Pruebas de Componentes**: Verifican el comportamiento de componentes React completos.
- **Pruebas de Snapshot**: Aseguran la estabilidad visual de los componentes de UI.

### Herramientas Utilizadas

- **Frontend**: Vitest, React Testing Library, Jest-DOM
- **Backend**: JUnit 5, Mockito
- **Bots**: Mocks de la API de Telegram

## 3. Casos de Prueba

### Frontend

#### Componente KPIs
- Verificación de métricas de equipo por sprint
- Verificación de métricas de desarrolladores por sprint
- Prueba de responsividad para pantallas pequeñas

#### Componente Tasks
- Renderizado del componente
- Mostrar número correcto de tareas
- Filtrado por descripción
- Edición de tareas
- Marcado de tareas como completadas
- Prueba de estabilidad visual (snapshot)

#### Integración Sprint-Developer
- Filtrado de tareas por desarrollador y sprint vía parámetros de consulta

### Backend

#### Controlador TelegramBot
- Creación de tareas vía comandos
- Visualización de tareas completadas por sprint
- Visualización de tareas completadas por usuario específico

#### Otros Controladores
- API de Usuarios
- API de Tareas
- API de Sprints
- API de Proyectos
- Controlador SPA

## 4. Resultados de Pruebas

### Resumen Ejecutivo

Todas las pruebas ejecutadas pasaron satisfactoriamente, demostrando la estabilidad y funcionalidad del sistema:
- 15 pruebas de backend: 100% exitosas
- 9 pruebas de frontend: 100% exitosas

### Detalle de Ejecución

#### Backend (Maven)
```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

#### Frontend (Vitest)
```
✓ Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
```

## 5. Comandos para Ejecutar Pruebas

### Backend (Spring Boot)
Para ejecutar todas las pruebas:
```
./mvnw test
```

Para ejecutar un test específico:
```
./mvnw test -Dtest="TelegramBotMockTest"
```

### Frontend (React)
Para ejecutar todas las pruebas:
```
npm test
```

Para ejecutar en modo watch:
```
npm test -- --watch
```

Para ejecutar un test específico:
```
npx vitest run KPIs.test.tsx
```

## 6. Recomendaciones

1. **Cobertura de Pruebas**: Aumentar la cobertura añadiendo pruebas para situaciones de error y casos límite.
2. **Pruebas E2E**: Implementar pruebas end-to-end que validen flujos completos de usuario.
3. **Integración Continua**: Configurar pipeline de CI/CD para ejecutar pruebas automáticamente en cada commit.
4. **Pruebas de Rendimiento**: Añadir pruebas de carga para validar el comportamiento bajo estrés.
5. **Documentación**: Mantener documentación actualizada de casos de prueba para facilitar mantenimiento futuro.

## 7. Conclusiones

El sistema de gestión de tareas con integración de Telegram demuestra un alto grado de estabilidad y funcionalidad. Las pruebas implementadas han validado tanto componentes individuales como integraciones clave, asegurando que el sistema funciona correctamente y es robusto ante cambios. La calidad del software se evidencia en los resultados exitosos de todas las pruebas ejecutadas. 

## 8. Test Traceability Matrix

### Resumen de las pruebas realizadas al proyecto
**Total de Pruebas realizadas: 24**

### Reporte detallado

#### Pruebas Backend (Java)

| TestId | Descripción de la prueba | Input | Output | Resultado | Responsable |
|--------|--------------------------|-------|--------|-----------|-------------|
| BE01 | Obtener todos los proyectos | Invocación al servicio findAll() | Lista de ProjectModel | PASS | |
| BE02 | Añadir proyecto | ProjectModel | ResponseEntity 200 OK | PASS | |
| BE03 | Obtener proyecto por ID inexistente | ID=999 | ResponseEntity 404 NOT_FOUND | PASS | |
| BE04 | Obtener sprint por ID | ID=1 | Sprint con ID=1 | PASS | |
| BE05 | Obtener sprint con ID de proyecto incorrecto | projectId=2, sprintId=1 | ResponseEntity 400 BAD_REQUEST | PASS | |
| BE06 | Obtener tarea por ID | taskId=1, projectId=1 | TaskModel con ID=1 | PASS | |
| BE07 | Obtener tarea con ID de proyecto incorrecto | projectId=2, taskId=1 | ResponseEntity 404 NOT_FOUND | PASS | |
| BE08 | Obtener usuario por ID | userId=1, projectId=1 | UserModel con ID=1 | PASS | |
| BE09 | Obtener usuario con ID de proyecto incorrecto | projectId=2, userId=1 | ResponseEntity 404 NOT_FOUND | PASS | |
| BE10 | Redirección SPA funcional | Invocación a forward() | "forward:/index.html" | PASS | |
| BE11 | Crear tarea mediante bot Telegram | TaskModel | Tarea creada y persistida | PASS | |
| BE12 | Listar tareas completadas en sprint | Consulta tareas "done" | Lista con 2 tareas completadas | PASS | |
| BE13 | Listar tareas asignadas a usuario | Consulta tareas usuario 42 | Lista con 2 tareas de usuario | PASS | |
| BE14 | Obtener tareas por estado | Consulta "done" | Lista de tareas completadas | PASS | |
| BE15 | Almacenar tarea con detalles | TaskModel | TaskModel persistido | PASS | |

#### Pruebas Frontend (React)

| TestId | Descripción de la prueba | Input | Output | Resultado | Responsable |
|--------|--------------------------|-------|--------|-----------|-------------|
| FE01 | Mostrar métricas de equipo por sprint | Vista KPI Dashboard | Tabla con métricas de Sprint 1 | PASS | |
| FE02 | Mostrar métricas de desarrollador | Vista Developers | Tabla con datos de Ana Ramírez | PASS | |
| FE03 | Comportamiento responsivo en pantallas pequeñas | window.innerWidth=500px | isXs=true | PASS | |
| FE04 | Comportamiento responsivo en pantallas grandes | window.innerWidth=800px | isXs=false | PASS | |
| FE05 | Renderizar componente Tasks | Cargar componente | Título "Tasks" visible | PASS | |
| FE06 | Mostrar número correcto de tareas | Cargar vista Tasks | Texto "2 tasks" visible | PASS | |
| FE07 | Filtrar tareas por búsqueda | Search="login" | Solo "Fix login bug" visible | PASS | |
| FE08 | Editar descripción de tarea | Nueva descripción | Actualización en API | PASS | |
| FE09 | Filtrar tareas por desarrollador y sprint | URL parámetros assignee=3&sprint=2 | Solo "Marcela Sprint2 Task" visible | PASS | | 