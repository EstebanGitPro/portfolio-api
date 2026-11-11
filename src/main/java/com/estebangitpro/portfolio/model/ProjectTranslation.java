package com.estebangitpro.portfolio.model;

import java.util.List;

/**
 * A project's copy in one additional language.
 *
 * <p>Every field is optional. Whatever a translation leaves out (null, blank or an
 * empty list) is shown in the project's base language by the client, so a project can
 * be translated piece by piece. Fields that do not depend on language — status, tags,
 * links, dates, images — live only on the {@link Project} itself.
 */
public record ProjectTranslation(
        String title,
        String summary,
        String description,
        List<TechStackItem> techStack,
        List<StrategyItem> strategies,
        List<String> learnings
) {
}
