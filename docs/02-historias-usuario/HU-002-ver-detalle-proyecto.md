# HU-002: Ver detalle de un proyecto

**Como** visitante del portfolio  
**Quiero** ver la página de detalle de un proyecto específico  
**Para** conocer a fondo el proyecto

**Criterios de aceptación:**
- [ ] `GET /api/projects/{slug}` devuelve el proyecto completo
- [ ] Incluye: todos los campos del proyecto (sin `email` ni datos privados)
- [ ] Si el slug no existe → 404
- [ ] Si el proyecto no está publicado → 404 (no existe para el público)

**Notas técnicas:**
- Endpoint: `GET /api/projects/{slug}`
- Controller: `PublicProjectController`
- ¿Requiere auth? No
