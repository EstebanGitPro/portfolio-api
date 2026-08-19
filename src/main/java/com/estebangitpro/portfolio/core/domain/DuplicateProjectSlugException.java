package com.estebangitpro.portfolio.core.domain;

/**
 * Raised when a project would take a slug another project already holds.
 *
 * <p>Slugs address projects in public URLs, so uniqueness is a business invariant, not a
 * storage detail — which is why this lives in the domain even though the database is
 * what detects the collision.
 */
public class DuplicateProjectSlugException extends RuntimeException {

    private final String slug;

    public DuplicateProjectSlugException(String slug) {
        super("A project already exists with slug: " + slug);
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }
}
