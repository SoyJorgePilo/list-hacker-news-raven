# List Hacker News

Aplicación Android nativa que permite ver, guardar y eliminar artículos de Hacker News, con soporte para visualización offline.

## Descripción

Esta aplicación ha sido desarrollada como parte de una prueba técnica, siguiendo el patrón de arquitectura MVVM (Model-View-ViewModel) y las mejores prácticas de desarrollo en Kotlin y Jetpack Compose. La aplicación permite a los usuarios:

- Visualizar un listado de artículos de Hacker News relacionados con Android
- Eliminar artículos mediante gestos de deslizamiento
- Ver el detalle de cada artículo en un navegador integrado
- Funcionar sin conexión, mostrando los datos previamente almacenados

## Tecnologías utilizadas

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Inyección de dependencias:** Hilt
- **Persistencia de datos:** Room
- **Networking:** Retrofit + Moshi
- **Programación asíncrona:** Coroutines + Flow
- **Navegación:** Jetpack Navigation Compose

## Estructura del proyecto

La aplicación se organiza siguiendo una arquitectura por capas, con clara separación de responsabilidades:

```
app/
├── data/
│   ├── model/             # Modelos de datos y mappers
│   ├── repository/        # Implementación del patrón repositorio
│   └── source/
│       ├── local/         # Base de datos Room y DAOs
│       └── remote/        # Cliente Retrofit para API
├── di/                    # Módulos de inyección de dependencias (Hilt)
├── navigation/            # Configuración de navegación
├── ui/
│   ├── articles/
│   │   ├── list/          # Pantalla de listado de artículos
│   │   └── detail/        # Pantalla de detalle del artículo
│   └── theme/             # Estilos y tema de la aplicación
└── util/                  # Clases utilitarias
```

## Funcionalidades implementadas

### 1. Listado de artículos

- Conexión a la API de Hacker News para obtener artículos recientes
- Implementación de patrón offline-first:
  - Se muestran datos locales inmediatamente si están disponibles
  - Se actualiza desde la red cuando hay conexión
  - Se proporciona retroalimentación visual sobre el estado de conexión
- Pull-to-refresh para actualizar manualmente la lista de artículos

### 2. Eliminación de artículos

- Implementación de gesto swipe-to-delete para eliminar artículos
- Uso de soft-delete para marcar artículos como eliminados en la base de datos
- Persistencia de eliminaciones incluso después de refrescar desde la API
- Opción de recuperar el artículo si se elimina por error (undo)

### 3. Detalle de artículos

- Visualización del contenido completo del artículo mediante WebView integrada
- Navegación fluida entre la lista y el detalle
- Manejo correcto del ciclo de vida de la WebView

### 4. Soporte offline

- Almacenamiento automático de artículos en base de datos local
- Detección de estado de conectividad para adaptar la experiencia de usuario
- Retroalimentación visual clara cuando se opera en modo offline
- Limitación de ciertas acciones (como refresh) cuando no hay conexión

## Patrones y prácticas implementados

- **Single Source of Truth:** El repositorio actúa como fuente única de verdad
- **Unidirectional Data Flow:** Los ViewModels exponen estados inmutables mediante StateFlow
- **Repository Pattern:** Abstracción de las fuentes de datos
- **Dependency Injection:** Inyección de dependencias para facilitar pruebas y desacoplamiento
- **Clean Architecture Principles:** Separación de capas y responsabilidades
- **Kotlin Idioms:** Uso de las características propias de Kotlin (extensions, coroutines, etc.)

## Instalación y ejecución

1. Clonar el repositorio
2. Abrir el proyecto en Android Studio
3. Sincronizar dependencias con Gradle
4. Ejecutar la aplicación en un emulador o dispositivo real

## Requerimientos

- Android 7.1 (API 25) o superior
- Android Studio Iguana o superior
- Kotlin 1.9.0 o superior
