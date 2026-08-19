package com.estebangitpro.portfolio.core.application.exception;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(String id) {
        super("Project not found with id: " + id);
    }
}
