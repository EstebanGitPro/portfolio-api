# Portfolio API Reference

**Base URL:** `http://localhost:8086`

---

## Public Endpoints

### GET /api/projects

List all published projects ordered by order field.

**Response:** `200 OK`

```json
[
  {
    "id": "64a1b2c3d4e5f6a7b8c9d0e1",
    "slug": "mi-portfolio-web",
    "title": "Mi Portfolio Web",
    "summary": "Sitio web personal con React y Spring Boot",
    "description": "Portfolio construido para mostrar mis proyectos como desarrollador",
    "status": "COMPLETADO",
    "tags": ["React", "Spring Boot", "MongoDB"],
    "thumbnail": "https://example.com/thumb.jpg",
    "coverImage": "https://example.com/cover.jpg",
    "videoUrl": "",
    "techStack": [
      {"name": "Spring Boot", "reason": "Backend robusto"},
      {"name": "MongoDB", "reason": "NoSQL flexible"}
    ],
    "strategies": [
      {"title": "Clean Architecture", "body": "Separación de responsabilidades"}
    ],
    "learnings": ["Docker multi-stage builds"],
    "links": {"github": "https://github.com/...", "live": "https://..."},
    "order": 1,
    "published": true,
    "startDate": "2025-01-01",
    "endDate": "2025-06-01",
    "createdAt": "2025-06-01T10:30:00",
    "updatedAt": "2025-06-01T10:30:00"
  }
]
```

### GET /api/projects/{id}

Get a single published project by ID.

**Response:** `200 OK` — single project object
**Error:** `404 Not Found` — `{"error": "Project not found with id: {id}"}`

---

## Admin Endpoints

### POST /api/admin/projects

Create a new project.

**Request Body:**

```json
{
  "title": "Mi Portfolio Web",
  "summary": "Sitio web personal con React y Spring Boot",
  "description": "Portfolio construido para mostrar mis proyectos como desarrollador",
  "status": "COMPLETADO",
  "tags": ["React", "Spring Boot", "MongoDB"],
  "thumbnail": "https://example.com/thumb.jpg",
  "coverImage": "https://example.com/cover.jpg",
  "videoUrl": "",
  "techStack": [
    {"name": "Spring Boot", "reason": "Backend robusto"},
    {"name": "MongoDB", "reason": "NoSQL flexible"}
  ],
  "strategies": [
    {"title": "Clean Architecture", "body": "Separación de responsabilidades"}
  ],
  "learnings": ["Docker multi-stage builds"],
  "links": {"github": "https://github.com/...", "live": "https://..."},
  "order": 1,
  "published": true,
  "startDate": "2025-01-01",
  "endDate": "2025-06-01"
}
```

**Response:** `201 Created` — project object with `id`, `slug`, `createdAt`, `updatedAt`
**Error:** `400 Bad Request` — validation errors if `title` is blank

---

### GET /api/admin/projects

List all projects including unpublished ones.

**Response:** `200 OK`

```json
[
  {
    "id": "64a1b2c3d4e5f6a7b8c9d0e1",
    "slug": "mi-portfolio-web",
    "title": "Mi Portfolio Web",
    "summary": "Sitio web personal con React y Spring Boot",
    "description": "Portfolio construido para mostrar mis proyectos como desarrollador",
    "status": "COMPLETADO",
    "tags": ["React", "Spring Boot", "MongoDB"],
    "thumbnail": "https://example.com/thumb.jpg",
    "coverImage": "https://example.com/cover.jpg",
    "videoUrl": "",
    "techStack": [
      {"name": "Spring Boot", "reason": "Backend robusto"},
      {"name": "MongoDB", "reason": "NoSQL flexible"}
    ],
    "strategies": [
      {"title": "Clean Architecture", "body": "Separación de responsabilidades"}
    ],
    "learnings": ["Docker multi-stage builds"],
    "links": {"github": "https://github.com/...", "live": "https://..."},
    "order": 1,
    "published": true,
    "startDate": "2025-01-01",
    "endDate": "2025-06-01",
    "createdAt": "2025-06-01T10:30:00",
    "updatedAt": "2025-06-01T10:30:00"
  }
]
```

---

### GET /api/admin/projects/{id}

Get any project by ID (including unpublished).

**Response:** `200 OK` — single project object
**Error:** `404 Not Found` — `{"error": "Project not found with id: {id}"}`

---

### PUT /api/admin/projects/{id}

Update an existing project. All fields are required.

**Request Body:**

```json
{
  "title": "Mi Portfolio Web - Updated",
  "summary": "Sitio web personal con React y Spring Boot",
  "description": "Portfolio construido para mostrar mis proyectos como desarrollador",
  "status": "COMPLETADO",
  "tags": ["React", "Spring Boot", "MongoDB"],
  "thumbnail": "https://example.com/thumb-v2.jpg",
  "coverImage": "https://example.com/cover-v2.jpg",
  "videoUrl": "",
  "techStack": [
    {"name": "Spring Boot", "reason": "Backend robusto"},
    {"name": "MongoDB", "reason": "NoSQL flexible"},
    {"name": "Docker", "reason": "Containerización"}
  ],
  "strategies": [
    {"title": "Clean Architecture", "body": "Separación de responsabilidades"},
    {"title": "TDD", "body": "Tests antes del código"}
  ],
  "learnings": ["Docker multi-stage builds", "MongoDB Atlas"],
  "links": {"github": "https://github.com/...", "live": "https://..."},
  "order": 1,
  "published": true,
  "startDate": "2025-01-01",
  "endDate": "2025-07-01"
}
```

**Response:** `200 OK` — updated project object
**Error:** `404 Not Found` — `{"error": "Project not found with id: {id}"}`

---

### DELETE /api/admin/projects/{id}

Delete a project by ID.

**Response:** `204 No Content`
**Error:** `404 Not Found` — `{"error": "Project not found with id: {id}"}`

---

## Field Reference

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| title | String | Yes | Cannot be blank. Used to auto-generate slug |
| summary | String | No | Short description |
| description | String | No | Full description |
| status | Enum | No | `EN_DESARROLLO`, `COMPLETADO`, `EN_PAUSA`, `ABANDONADO` |
| tags | List\<String\> | No | Tech or category tags |
| thumbnail | String | No | URL to thumbnail image |
| coverImage | String | No | URL to cover image |
| videoUrl | String | No | URL to demo video |
| techStack | List\<Object\> | No | `{"name": "...", "reason": "..."}` |
| strategies | List\<Object\> | No | `{"title": "...", "body": "..."}` |
| learnings | List\<String\> | No | Key takeaways |
| links | Object | No | `{"github": "...", "live": "..."}` |
| order | int | No | Display order (default: 0) |
| published | boolean | No | Show in public endpoint (default: true) |
| startDate | LocalDate | No | Format: `YYYY-MM-DD` |
| endDate | LocalDate | No | Format: `YYYY-MM-DD` |

---

## Error Responses

### 400 Bad Request (Validation)

```json
{
  "error": "Validation failed",
  "details": [
    {"field": "title", "message": "must not be blank"}
  ]
}
```

### 404 Not Found

```json
{
  "error": "Project not found with id: 64a1b2c3d4e5f6a7b8c9d0e1"
}
```

### 500 Internal Server Error

```json
{
  "error": "Internal server error"
}
```
