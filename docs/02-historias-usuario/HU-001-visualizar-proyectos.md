# HU-001: Visualizar proyectos en cuadrícula

**Como** visitante del portfolio  
**Quiero** ver una cuadrícula con todos los proyectos publicados  
**Para** conocer el trabajo del desarrollador

**Criterios de aceptación:**
- [ ] `GET /api/projects` devuelve lista de proyectos con `published: true`
- [ ] Ordenados por el campo `order` ascendente
- [ ] Cada item contiene: `slug`, `title`, `summary`, `thumbnail`, `tags`
- [ ] Proyectos con `published: false` NO aparecen
- [ ] Respuesta con status 200 OK

**Notas técnicas:**
- Endpoint: `GET /api/projects`
- Controller: `PublicProjectController`
- ¿Requiere auth? No
