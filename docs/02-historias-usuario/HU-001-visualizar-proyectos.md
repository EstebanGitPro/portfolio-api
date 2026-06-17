# HU-001: Visualizar proyectos en cuadrícula

**Como** visitante del portfolio  
**Quiero** ver una cuadrícula con todos los proyectos publicados  
**Para** conocer el trabajo del desarrollador

**Criterios de aceptación:**
- [x] `GET /api/projects` devuelve lista de proyectos con `published: true`
- [x] Ordenados por el campo `order` ascendente
- [x] Cada item contiene: `slug`, `title`, `summary`, `thumbnail`, `tags`
- [x] Proyectos con `published: false` NO aparecen
- [x] Respuesta con status 200 OK

**Notas técnicas:**
- Endpoint: `GET /api/projects`
- Controller: `PublicProjectController`
- ¿Requiere auth? No
