package de.htw_berlin.kindercare.config;

import de.htw_berlin.kindercare.child.Child;
import de.htw_berlin.kindercare.child.ChildRepository;
import de.htw_berlin.kindercare.medication.Medication;
import de.htw_berlin.kindercare.medication.MedicationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
// Demo data is useful locally, but production must never modify an existing Render database at startup.
@Profile("local")
public class SampleDataConfig {
    @Bean
    CommandLineRunner addExamples(ChildRepository children, MedicationRepository medications) {
        return arguments -> {
            if (children.count() == 0) children.save(new Child("Emma Muller", "Peanuts"));
            if (medications.count() == 0) medications.save(new Medication("Salbutamol", "Emma Muller", "1 puff"));
        };
    }
}
