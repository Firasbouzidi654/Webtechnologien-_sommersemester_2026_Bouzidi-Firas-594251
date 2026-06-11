package de.htw_berlin.KinderCareConnect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "medications")
public class MedicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String medicationId;
    private Long childId;
    private String name;
    private String activeIngredient;
    private String dosage;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    private boolean prescriptionUploaded;
    private String todayStatus;
    private String scheduledTime;
    private LocalDate scheduledDate;
    private String frequency;
    private String dayPart;

    @NonNull
    public Long getId() { return id; }

    public String getMedicationId() { return medicationId; }
    public void setMedicationId(String medicationId) { this.medicationId = medicationId; }

    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getActiveIngredient() { return activeIngredient; }
    public void setActiveIngredient(String activeIngredient) { this.activeIngredient = activeIngredient; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public boolean isPrescriptionUploaded() { return prescriptionUploaded; }
    public void setPrescriptionUploaded(boolean prescriptionUploaded) { this.prescriptionUploaded = prescriptionUploaded; }

    public String getTodayStatus() { return todayStatus; }
    public void setTodayStatus(String todayStatus) { this.todayStatus = todayStatus; }

    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getDayPart() { return dayPart; }
    public void setDayPart(String dayPart) { this.dayPart = dayPart; }
}
