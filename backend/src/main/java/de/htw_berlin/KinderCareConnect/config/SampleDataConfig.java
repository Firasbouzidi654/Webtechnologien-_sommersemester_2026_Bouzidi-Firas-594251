package de.htw_berlin.KinderCareConnect.config;

import de.htw_berlin.KinderCareConnect.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.repository.ChildRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.List;

@Configuration
@Profile({"postgresql", "default"})
public class SampleDataConfig {

    // Only runs with the postgresql (local dev) or default profile.
    // Production profile skips this so the database starts clean.
    @Bean
    CommandLineRunner loadSampleChildren(ChildRepository childRepository) {
        return args -> {
            if (childRepository.count() > 0) {
                return;
            }

            childRepository.saveAll(List.of(
                new ChildEntity(
                    "Adam Schneider",
                    LocalDate.of(2021, 3, 15),
                    "Peanuts",
                    "Mild asthma",
                    "",
                    "Sara Schneider: +49 151 123456",
                    "Sunflowers",
                    "Sara Schneider",
                    "sara.schneider@example.com",
                    "Needs inhaler available during outdoor play."
                ),
                new ChildEntity(
                    "Noah Becker",
                    LocalDate.of(2020, 7, 22),
                    "None known",
                    "Asthma",
                    "",
                    "Jonas Becker: +49 152 987654",
                    "Sunflowers",
                    "Jonas Becker",
                    "jonas.becker@example.com",
                    "Observe breathing after sports activities."
                ),
                new ChildEntity(
                    "Lina Wagner",
                    LocalDate.of(2021, 1, 10),
                    "Bee stings",
                    "None",
                    "",
                    "Amira Wagner: +49 176 456789",
                    "Rainbows",
                    "Amira Wagner",
                    "amira.wagner@example.com",
                    "Emergency spray is stored in the office cabinet."
                )
            ));
        };
    }
}
