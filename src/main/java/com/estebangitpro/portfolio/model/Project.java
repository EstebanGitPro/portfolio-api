package com.estebangitpro.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(value = "projects")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Project {

    @Id
    private String id;

    /**
     * slug: Identificador único legible para humanos que se usa en la URL.
     * En vez de /projects/123abc, usas /projects/mi-proyecto.
     * Se genera desde el título: "Mi Proyecto" → "mi-proyecto"
     */
    private String slug;

    /**
     * title: Nombre visible del proyecto en la UI.
     * Aparece en la card del grid y en el encabezado del detalle.
     */
    private String title;

    /**
     * summary: Descripción corta (1-2 líneas) que se muestra en la card
     * del grid de proyectos. No es lo mismo que description.
     */
    private String summary;

    /**
     * description: Descripción larga y detallada del proyecto.
     * Se muestra en la página de detalle del proyecto.
     */
    private String description;

    /**
     * status: Estado actual del proyecto.
     * "completed"  → terminado, visible en portfolio
     * "in_progress" → en desarrollo, con badge de "En progreso"
     * "planned"     → planeado, aún no empezado
     */
    private String status;

    /**
     * tags: Etiquetas o categorías para filtrar proyectos.
     * Ej: ["React", "Spring Boot", "MongoDB"]
     */
    private List<String> tags;

    /**
     * thumbnail: URL de la imagen miniatura del proyecto.
     * Se muestra en la card del grid (vista previa pequeña).
     */
    private String thumbnail;

    /**
     * coverImage: URL de la imagen grande de cabecera.
     * Se muestra en la parte superior de la página de detalle.
     */
    private String coverImage;

    /**
     * videoUrl: URL opcional de un video demo (YouTube, Vimeo).
     * Si está presente, se muestra un reproductor en el detalle.
     */
    private String videoUrl;

    /**
     * techStack: Lista de tecnologías usadas y por qué las elegiste.
     * Cada entrada tiene: nombre de la tecnología y la razón de uso.
     * Ej: [{ name: "React", reason: "Por su ecosistema" }]
     */
    private List<TechStackItem> techStack;

    /**
     * strategies: Decisiones técnicas o de diseño que tomaste.
     * Cada entrada tiene un título y una explicación.
     * Ej: [{ title: "Por qué MongoDB", body: "..." }]
     */
    private List<StrategyItem> strategies;

    /**
     * learnings: Lista de aprendizajes clave que te llevó este proyecto.
     * Se muestra como bullets en el detalle.
     * Ej: ["Aprendí a manejar estados globales", "Mejoré en testing"]
     */
    private List<String> learnings;

    /**
     * links: Objeto con las URLs del proyecto.
     * github → repositorio
     * live → sitio en producción
     * demo → demo en vivo (si aplica)
     */
    private ProjectLinks links;

    /**
     * order: Número para controlar el orden de aparición en el grid.
     * 1 aparece primero, 2 después, etc.
     */
    private Integer order;

    /**
     * published: Controla si el proyecto es visible en el portfolio.
     * true → visible al público
     * false → oculto (borrador)
     */
    private Boolean published;

    private Date startDate;
    private Date endDate;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;

}
