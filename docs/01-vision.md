# Portfolio API — Visión del Proyecto

## ¿Qué es?

API REST para un portfolio personal de desarrollador. Expone proyectos, permite comentarios y recolecta analytics básicos.

## Stack técnico

- **Java 25** + **Spring Boot 4.0.6**
- **MongoDB** como base de datos
- **Maven** para build
- **Testcontainers** para tests de integración (próximamente)

## Módulos planificados

| Módulo | Descripción |
|---|---|
| Proyectos | CRUD de proyectos del portfolio (público + admin) |
| Comentarios | Sistema de comentarios por proyecto |
| Analytics | Visitas y eventos |
| Admin | Panel oculto para gestionar contenido |

## Dominio

- `estebangitpro.com` (pendiente definir)

## Estilo arquitectónico

- REST API tradicional (no Reactivo)
- Capas: Controller → Service → Repository → MongoDB
- DTOs para request/response
- MapStruct para mapeos
