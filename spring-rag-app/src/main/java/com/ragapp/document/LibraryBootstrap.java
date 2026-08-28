package com.ragapp.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Prepares the document library on startup.
 *
 * <p>If a persisted library exists on disk it is restored (seeds + all previously
 * uploaded documents). Otherwise the bundled organization seeds are loaded and
 * the result is persisted, becoming the baseline for future restarts.
 */
@Component
public class LibraryBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LibraryBootstrap.class);

    private final LibraryPersistence persistence;
    private final OrganizationLibrarySeeder seeder;

    public LibraryBootstrap(LibraryPersistence persistence, OrganizationLibrarySeeder seeder) {
        this.persistence = persistence;
        this.seeder = seeder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (persistence.restore()) {
            return;
        }
        seeder.seedAll();
        persistence.persist();
        if (persistence.isEnabled()) {
            log.info("Seeded a fresh library and persisted it to disk.");
        }
    }
}
