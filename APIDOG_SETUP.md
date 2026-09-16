# Guía para ingresar proyectos ficticios en Apidog

## URL del Endpoint

**Base URL:** `http://localhost:8086` (asumiendo que el servidor está corriendo localmente)

**Endpoint:** `POST /api/admin/projects`

**URL completa:** `http://localhost:8086/api/admin/projects`

---

## Instrucciones en Apidog

1. **Crear una nueva request**
   - Click en `+` o `Create New`
   - Selecciona `HTTP` request

2. **Configurar la request**
   - **Method:** `POST`
   - **URL:** `http://localhost:8086/api/admin/projects`
   - **Body Type:** `JSON`

3. **Pegar el JSON**
   - Copiar uno de los payloads ficticios de abajo
   - Pegar en la sección "Body"

4. **Enviar**
   - Click en "Send" o presionar `Ctrl+Enter`
   - La respuesta debe ser `201 Created` con los datos del proyecto

---

## Proyectos ficticios para ingresarlos

### Proyecto 1: Blog de Viajes en React

```json
{
  "title": "Travel Blog Platform",
  "summary": "Plataforma de blog interactivo para compartir experiencias de viaje",
  "description": "Aplicación full-stack que permite a usuarios crear, editar y compartir historias de viaje con fotos, mapas interactivos y recomendaciones. Incluye sistema de comentarios y likes en tiempo real.",
  "status": "COMPLETADO",
  "tags": ["React", "Node.js", "MongoDB", "Real-time"],
  "thumbnail": "https://images.unsplash.com/photo-1488646953014-85cb44e25828",
  "coverImage": "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1",
  "videoUrl": "https://www.youtube.com/embed/dQw4w9WgXcQ",
  "techStack": [
    {
      "name": "React 18",
      "reason": "UI dinámica y responsive con hooks"
    },
    {
      "name": "Node.js + Express",
      "reason": "Backend escalable con middleware customizado"
    },
    {
      "name": "MongoDB",
      "reason": "Base de datos flexible para contenido variado"
    },
    {
      "name": "WebSockets",
      "reason": "Actualizaciones en tiempo real"
    }
  ],
  "strategies": [
    {
      "title": "Arquitectura hexagonal",
      "body": "Separación clara entre lógica de negocio y presentación"
    },
    {
      "title": "Lazy loading de imágenes",
      "body": "Optimización de performance para galerías"
    }
  ],
  "learnings": [
    "Implementación de WebSockets para chat en vivo",
    "Optimización de consultas MongoDB con índices",
    "Testing end-to-end con Cypress"
  ],
  "links": {
    "github": "https://github.com/EstebanGitPro/travel-blog",
    "live": "https://travel-blog.example.com"
  },
  "order": 1,
  "published": true,
  "startDate": "2025-01-15",
  "endDate": "2025-06-30"
}
```

### Proyecto 2: Dashboard de Analytics

```json
{
  "title": "Real-time Analytics Dashboard",
  "summary": "Dashboard ejecutivo para visualizar métricas de negocio en tiempo real",
  "description": "Sistema de análisis que recopila datos de múltiples fuentes, genera reportes automáticos y proporciona visualizaciones interactivas. Incluye predicciones con machine learning básico.",
  "status": "EN_PROGRESO",
  "tags": ["Python", "Pandas", "Plotly", "API"],
  "thumbnail": "https://images.unsplash.com/photo-1551288049-bebda4e38f71",
  "coverImage": "https://images.unsplash.com/photo-1504384308090-c894fdcc538d",
  "videoUrl": null,
  "techStack": [
    {
      "name": "Python + FastAPI",
      "reason": "API rápida y documentación automática con OpenAPI"
    },
    {
      "name": "Pandas",
      "reason": "Procesamiento y transformación de datos"
    },
    {
      "name": "Plotly",
      "reason": "Gráficos interactivos y responsive"
    },
    {
      "name": "PostgreSQL",
      "reason": "Almacenamiento robusto de métricas"
    }
  ],
  "strategies": [
    {
      "title": "Caché distribuido",
      "body": "Redis para cachear reportes frecuentes"
    },
    {
      "title": "Procesamiento asíncrono",
      "body": "Celery para jobs de larga duración"
    }
  ],
  "learnings": [
    "Implementación de WebSockets en FastAPI",
    "Optimización de queries con EXPLAIN ANALYZE",
    "Deploy en Kubernetes"
  ],
  "links": {
    "github": "https://github.com/EstebanGitPro/analytics-dashboard",
    "live": "https://analytics.example.com"
  },
  "order": 2,
  "published": true,
  "startDate": "2025-04-01",
  "endDate": null
}
```

### Proyecto 3: Mobile App de Gestión de Tareas

```json
{
  "title": "Task Manager Mobile App",
  "summary": "Aplicación móvil para gestionar tareas diarias con sincronización en la nube",
  "description": "App iOS/Android que permite crear, editar y organizar tareas en tiempo real. Incluye recordatorios, categorías, prioridades y sincronización automática entre dispositivos.",
  "status": "COMPLETADO",
  "tags": ["Flutter", "Firebase", "Mobile", "Cloud"],
  "thumbnail": "https://images.unsplash.com/photo-1517694712202-14dd9538aa97",
  "coverImage": "https://images.unsplash.com/photo-1611532736595-de2c0ecc0b0a",
  "videoUrl": "https://www.youtube.com/embed/dQw4w9WgXcQ",
  "techStack": [
    {
      "name": "Flutter",
      "reason": "Cross-platform con excelente performance"
    },
    {
      "name": "Firebase Realtime DB",
      "reason": "Sincronización en tiempo real sin backend"
    },
    {
      "name": "Riverpod",
      "reason": "State management robusto y escalable"
    }
  ],
  "strategies": [
    {
      "title": "Offline-first",
      "body": "Datos guardados localmente y sincronización cuando hay conexión"
    },
    {
      "title": "Atomic design",
      "body": "Componentes reutilizables y mantenibles"
    }
  ],
  "learnings": [
    "Manejo de sincronización offline-first",
    "Notificaciones push con FCM",
    "Testing de widgets en Flutter"
  ],
  "links": {
    "github": "https://github.com/EstebanGitPro/task-manager-app",
    "live": "https://play.google.com/store/apps/details?id=com.example.taskmanager"
  },
  "order": 3,
  "published": true,
  "startDate": "2024-09-01",
  "endDate": "2025-02-28"
}
```

### Proyecto 4: Sistema de E-commerce

```json
{
  "title": "E-commerce Platform con Checkout",
  "summary": "Plataforma de venta online con carrito, pagos y gestión de inventario",
  "description": "Sistema completo de e-commerce con catálogo de productos, carrito de compras, integración de pagos Stripe, gestión de pedidos y panel de administración.",
  "status": "COMPLETADO",
  "tags": ["Next.js", "Stripe", "Prisma", "TypeScript"],
  "thumbnail": "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f",
  "coverImage": "https://images.unsplash.com/photo-1461896836934-ffe607ba8211",
  "videoUrl": null,
  "techStack": [
    {
      "name": "Next.js 14",
      "reason": "SSR, SSG y API routes en un solo framework"
    },
    {
      "name": "Prisma ORM",
      "reason": "Operaciones con BD tipo-seguras"
    },
    {
      "name": "Stripe API",
      "reason": "Pagos seguros y PCI compliant"
    },
    {
      "name": "Tailwind CSS",
      "reason": "Estilos utility-first y responsive"
    }
  ],
  "strategies": [
    {
      "title": "Arquitectura de capas",
      "body": "Controllers → Services → Repositories"
    },
    {
      "title": "Validación con Zod",
      "body": "Schemas de validación type-safe end-to-end"
    }
  ],
  "learnings": [
    "Integración segura de Stripe con webhooks",
    "Manejo de transacciones en Prisma",
    "SEO en Next.js con metadata"
  ],
  "links": {
    "github": "https://github.com/EstebanGitPro/ecommerce-platform",
    "live": "https://ecommerce.example.com"
  },
  "order": 4,
  "published": true,
  "startDate": "2024-06-01",
  "endDate": "2024-12-31"
}
```

---

## Notas

- El `slug` se genera automáticamente desde el `title` (ej: "Travel Blog Platform" → "travel-blog-platform")
- Los campos `id`, `createdAt` y `updatedAt` los asigna el servidor
- El `status` debe ser uno de: `COMPLETADO`, `EN_PROGRESO`, `PLANEADO`, `ARCHIVADO`
- Los campos opcionales como `videoUrl` pueden ser `null`
- Si hay un error `409`, significa que ya existe un proyecto con ese slug
