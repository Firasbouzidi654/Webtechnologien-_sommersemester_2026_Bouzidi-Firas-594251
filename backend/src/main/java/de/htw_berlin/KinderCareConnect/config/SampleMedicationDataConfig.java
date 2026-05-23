package de.htw_berlin.KinderCareConnect.config;

import de.htw_berlin.KinderCareConnect.persistence.entity.ChildEntity;
import de.htw_berlin.KinderCareConnect.persistence.entity.EmergencyContactEntity;
import de.htw_berlin.KinderCareConnect.persistence.entity.MedicationEntity;
import de.htw_berlin.KinderCareConnect.persistence.repository.ChildRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.EmergencyContactRepository;
import de.htw_berlin.KinderCareConnect.persistence.repository.MedicationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;

@Configuration
public class SampleMedicationDataConfig {

    @Bean
    @Order(2)
    CommandLineRunner loadSampleMedicationsAndContacts(
        ChildRepository childRepository,
        MedicationRepository medicationRepository,
        EmergencyContactRepository emergencyContactRepository
    ) {
        return args -> {
            if (medicationRepository.count() > 0) {
                return;
            }

            List<ChildEntity> children = childRepository.findAllByOrderByIdAsc();
            if (children.isEmpty()) {
                return;
            }

            Map<String, ChildEntity> byName = new java.util.HashMap<>();
            for (ChildEntity c : children) {
                byName.put(c.getName(), c);
            }

            ChildEntity adam = byName.get("Adam Schneider");
            ChildEntity noah = byName.get("Noah Becker");
            ChildEntity lina = byName.get("Lina Wagner");

            if (adam != null) {
                emergencyContactRepository.saveAll(List.of(
                    new EmergencyContactEntity(adam.getId(), "Sara Schneider", "Mother", "+49 151 123456", "sara.schneider@example.com", 1),
                    new EmergencyContactEntity(adam.getId(), "Paul Schneider", "Father", "+49 152 222333", "paul.schneider@example.com", 2)
                ));
                medicationRepository.save(medication("MED-001", adam.getId(), "Asthma Spray", "Salbutamol", "1 puff", "Shake before use.", "08:30", "Morning"));
            }

            if (noah != null) {
                emergencyContactRepository.saveAll(List.of(
                    new EmergencyContactEntity(noah.getId(), "Jonas Becker", "Father", "+49 152 987654", "jonas.becker@example.com", 1),
                    new EmergencyContactEntity(noah.getId(), "Mina Becker", "Mother", "+49 157 444555", "mina.becker@example.com", 2)
                ));
                medicationRepository.save(medication("MED-002", noah.getId(), "Asthma Spray", "Salbutamol", "1 puff", "Use spacer and monitor breathing.", "10:00", "Specific time"));
                medicationRepository.save(medication("MED-003", noah.getId(), "Vitamin D Drops", "Colecalciferol", "2 drops", "Give after snack.", "12:30", "Daily"));
            }

            if (lina != null) {
                emergencyContactRepository.save(
                    new EmergencyContactEntity(lina.getId(), "Amira Wagner", "Mother", "+49 176 456789", "amira.wagner@example.com", 1)
                );
                medicationRepository.save(medication("MED-004", lina.getId(), "Allergy Spray", "Antihistamine", "1 spray", "Use only if symptoms appear.", "09:15", "Morning"));
            }
        };
    }

    private MedicationEntity medication(
        String medicationId, Long childId, String name,
        String activeIngredient, String dosage, String instructions,
        String scheduledTime, String dayPart
    ) {
        MedicationEntity med = new MedicationEntity();
        med.setMedicationId(medicationId);
        med.setChildId(childId);
        med.setName(name);
        med.setActiveIngredient(activeIngredient);
        med.setDosage(dosage);
        med.setInstructions(instructions);
        med.setScheduledTime(scheduledTime);
        med.setDayPart(dayPart);
        med.setFrequency("Daily");
        med.setTodayStatus("Pending");
        med.setPrescriptionUploaded(true);
        med.setQrPayload("kindercare-connect:medication:" + medicationId);
        return med;
    }
}
