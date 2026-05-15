package de.htw_berlin.KinderCareConnect.config;

import de.htw_berlin.KinderCareConnect.persistence.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.persistence.repository.ChildRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class SampleDataConfig {

    @Bean
    CommandLineRunner loadSampleChildren(ChildRepository childRepository) {
        return args -> {
            if (childRepository.count() > 0) {
                return;
            }

            childRepository.saveAll(List.of(
                    new ChildEntity(
                            "Anna Schmidt",
                            LocalDate.of(2020, 3, 15),
                            "Peanuts, Milk",
                            "Mild Asthma",
                            "None",
                            "Parents: +49-123-456789"
                    ),
                    new ChildEntity(
                            "Max Weber",
                            LocalDate.of(2019, 7, 22),
                            "Nuts",
                            "Diabetes Type 1",
                            "ADHD",
                            "Parents: +49-987-654321"
                    ),
                    new ChildEntity(
                            "Emma Fischer",
                            LocalDate.of(2021, 1, 10),
                            "Lactose",
                            "None",
                            "Autism",
                            "Parents: +49-555-888444"
                    )
            ));
        };
    }
}