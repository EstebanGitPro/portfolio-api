# Estándares del equipo

## Git Flow

```
main         → producción (solo merges desde develop)
  develop    → integración
    feature/*  → nuevas funcionalidades
    fix/*      → correcciones
```

## Convención de commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: HU-003 crear proyecto endpoint
fix: corregir validación de slug duplicado
docs: agregar HU-004 a la documentación
refactor: extraer lógica de slug a helper
```

## Nombramiento

- **Clases**: PascalCase (`ProjectService`, `ProjectRequestDTO`)
- **Métodos/variables**: camelCase (`findBySlug`, `getAllProjects`)
- **Archivos**: mismo nombre que la clase
- **Package**: `com.estebangitpro.portfolio.{capa}`
- **Colecciones MongoDB**: plural en inglés (`projects`, `comments`)

## Pull Requests

- Título descriptivo con número de HU si aplica
- Máximo 400 líneas por PR
- Siempre apuntar a `develop`
- Requiere al menos 1 approve
