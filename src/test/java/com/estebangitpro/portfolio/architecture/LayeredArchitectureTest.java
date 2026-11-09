package com.estebangitpro.portfolio.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the dependency rule of layered architecture: dependencies flow downwards.
 *
 * <pre>
 *   controller (web layer)
 *        ↓
 *   service (business logic)
 *        ↓
 *   repository (data access) + model (domain objects)
 *        ↓
 *   external (external integrations, isolated)
 * </pre>
 *
 * The model layer is the innermost: it contains pure domain objects free of framework dependencies.
 * Controller depends on service, service on repository/model, repository on model.
 * External layer is isolated and only consumed by service/controller when needed.
 */
@AnalyzeClasses(
        packages = LayeredArchitectureTest.ROOT,
        importOptions = ImportOption.DoNotIncludeTests.class)
public class LayeredArchitectureTest {

    static final String ROOT = "com.estebangitpro.portfolio";

    private static final String CONTROLLER = "..controller..";
    private static final String SERVICE = "..service..";
    private static final String REPOSITORY = "..repository..";
    private static final String MODEL = "..model..";
    private static final String EXTERNAL = "..external..";
    private static final String CONFIG = "..config..";
    private static final String DTO = "..dto..";
    private static final String MAPPER = "..mapper..";
    private static final String EXCEPTION = "..exception..";

    @ArchTest
    public static final ArchRule layered_dependencies_flow_downwards = layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage(ROOT + "..")
            .layer("Controller").definedBy(CONTROLLER)
            .layer("Service").definedBy(SERVICE)
            .layer("Repository").definedBy(REPOSITORY)
            .layer("Model").definedBy(MODEL)
            .layer("External").definedBy(EXTERNAL)
            .layer("Config").definedBy(CONFIG)
            .layer("DTO").definedBy(DTO)
            .layer("Mapper").definedBy(MAPPER)
            .layer("Exception").definedBy(EXCEPTION)

            .whereLayer("Config").mayNotBeAccessedByAnyLayer()
            .whereLayer("Controller").mayOnlyBeAccessedByLayers("Config")
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Config")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service", "Controller", "Config")
            .whereLayer("External").mayOnlyBeAccessedByLayers("Service", "Controller", "Config")
            .whereLayer("DTO").mayOnlyBeAccessedByLayers("Controller", "Service", "Mapper", "Config")
            .whereLayer("Mapper").mayOnlyBeAccessedByLayers("Controller", "Service", "Config")
            .whereLayer("Exception").mayOnlyBeAccessedByLayers("Controller", "Service", "Repository", "Config");

    @ArchTest
    public static final ArchRule model_does_not_depend_on_layers_above = noClasses()
            .that().resideInAPackage(MODEL)
            .should().dependOnClassesThat().resideInAnyPackage(CONTROLLER, SERVICE, REPOSITORY,
                    EXTERNAL, CONFIG, DTO, MAPPER)
            .because("the model is the innermost layer: it contains only domain objects");

    @ArchTest
    public static final ArchRule repository_does_not_depend_on_controller = noClasses()
            .that().resideInAPackage(REPOSITORY)
            .should().dependOnClassesThat().resideInAPackage(CONTROLLER)
            .because("repository accesses data; it never calls controller");

    @ArchTest
    public static final ArchRule service_does_not_depend_on_controller = noClasses()
            .that().resideInAPackage(SERVICE)
            .should().dependOnClassesThat().resideInAPackage(CONTROLLER)
            .because("service implements business logic; it never calls controller");

    @ArchTest
    public static final ArchRule external_is_isolated_from_repository = noClasses()
            .that().resideInAPackage(EXTERNAL)
            .should().dependOnClassesThat().resideInAPackage(REPOSITORY)
            .because("external is a separate concern and should not depend on data access");

    @ArchTest
    public static final ArchRule model_is_free_of_frameworks = noClasses()
            .that().resideInAPackage(MODEL)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "javax..",
                    "org.mapstruct..",
                    "com.mongodb..",
                    "org.bson..",
                    "io.swagger..",
                    "org.springdoc..")
            .because("the model must stay pure: no web, no persistence, no DI container");

    @ArchTest
    public static final ArchRule spring_data_stays_in_the_repository = noClasses()
            .that().resideOutsideOfPackages(REPOSITORY, CONFIG)
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data..")
            .because("persistence technology is a repository implementation detail");

}
