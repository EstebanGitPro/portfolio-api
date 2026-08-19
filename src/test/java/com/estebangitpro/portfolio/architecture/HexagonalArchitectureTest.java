package com.estebangitpro.portfolio.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the dependency rule of hexagonal architecture: dependencies always point inwards.
 *
 * <pre>
 *   adapter.in  (web, bootstrap)  ->  application.port.in   ->  application.service  ->  domain
 *   adapter.out (persistence, geo, parser) implements application.port.out
 * </pre>
 *
 * The domain is the centre: it knows nothing about use cases, HTTP, MongoDB, Spring or MapStruct.
 */
@AnalyzeClasses(
        packages = HexagonalArchitectureTest.ROOT,
        importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    static final String ROOT = "com.estebangitpro.portfolio";

    private static final String DOMAIN = "..core.domain..";
    private static final String APPLICATION = "..core.application..";
    private static final String CORE = "..core..";
    private static final String ADAPTER = "..adapter..";
    private static final String DRIVING_ADAPTER = "..adapter.in..";
    private static final String DRIVEN_ADAPTER = "..adapter.out..";
    private static final String CONFIG = "..config..";
    private static final String PORTS = "..core.application.port..";
    private static final String IN_PORTS = "..core.application.port.in..";
    private static final String OUT_PORTS = "..core.application.port.out..";

    @ArchTest
    static final ArchRule onion_layers_only_point_inwards = layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage(ROOT + "..")
            .layer("Bootstrap").definedBy(ROOT)
            .layer("Configuration").definedBy(CONFIG)
            .layer("DrivingAdapters").definedBy(DRIVING_ADAPTER)
            .layer("DrivenAdapters").definedBy(DRIVEN_ADAPTER)
            .layer("Application").definedBy(APPLICATION)
            .layer("Domain").definedBy(DOMAIN)

            .whereLayer("Bootstrap").mayNotBeAccessedByAnyLayer()
            .whereLayer("Configuration").mayNotBeAccessedByAnyLayer()
            .whereLayer("DrivingAdapters").mayNotBeAccessedByAnyLayer()
            .whereLayer("DrivenAdapters").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers(
                    "DrivingAdapters", "DrivenAdapters", "Configuration", "Bootstrap")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers(
                    "Application", "DrivingAdapters", "DrivenAdapters", "Configuration", "Bootstrap");

    @ArchTest
    static final ArchRule domain_does_not_depend_on_application = noClasses()
            .that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAPackage(APPLICATION)
            .because("the domain is the innermost ring: it must be describable without use cases");

    @ArchTest
    static final ArchRule core_does_not_depend_on_adapters = noClasses()
            .that().resideInAPackage(CORE)
            .should().dependOnClassesThat().resideInAnyPackage(ADAPTER, CONFIG)
            .because("the core is the centre: adapters depend on it, never the other way around");

    @ArchTest
    static final ArchRule domain_is_free_of_frameworks = noClasses()
            .that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "javax..",
                    "org.mapstruct..",
                    "com.mongodb..",
                    "org.bson..")
            .because("the domain must stay pure: no web, no persistence, no DI container");

    @ArchTest
    static final ArchRule driving_adapters_only_enter_through_in_ports = noClasses()
            .that().resideInAPackage(DRIVING_ADAPTER)
            .should().dependOnClassesThat().resideInAPackage(OUT_PORTS)
            .because("a driving adapter drives the application through a use case; "
                    + "reaching an out-port skips every business rule the use case applies");

    @ArchTest
    static final ArchRule driven_adapters_only_implement_out_ports = noClasses()
            .that().resideInAPackage(DRIVEN_ADAPTER)
            .should().dependOnClassesThat().resideInAPackage(IN_PORTS)
            .because("a driven adapter is called by the application; it never calls use cases back");

    @ArchTest
    static final ArchRule driving_adapters_do_not_depend_on_driven_adapters = noClasses()
            .that().resideInAPackage(DRIVING_ADAPTER)
            .should().dependOnClassesThat().resideInAPackage(DRIVEN_ADAPTER)
            .because("the web adapter reaches persistence through the core, never directly");

    @ArchTest
    static final ArchRule spring_data_stays_in_the_persistence_adapter = noClasses()
            .that().resideOutsideOfPackages("..adapter.out.persistence..", CONFIG)
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data..")
            .because("persistence technology is a driven-adapter detail");

    @ArchTest
    static final ArchRule ports_are_interfaces_or_models = classes()
            .that().resideInAPackage(PORTS).and().areTopLevelClasses()
            .should().beInterfaces()
            .orShould().beRecords()
            .because("a port is either a contract or the model that contract exchanges");
}
