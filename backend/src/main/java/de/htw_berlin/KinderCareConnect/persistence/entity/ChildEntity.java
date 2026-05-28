package de.htw_berlin.KinderCareConnect.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "children")
public class ChildEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDate dateOfBirth;

    private String allergies;

    private String chronicDiseases;

    private String specialNeeds;

    private String emergencyContact;

    private String groupName;

    private String parentName;

    private String parentEmail;

    @Column(columnDefinition = "TEXT")
    private String healthNotes;

    @Column(columnDefinition = "TEXT")
    private String photoUrl;

    public ChildEntity() {
    }

    public ChildEntity(
            String name,
            LocalDate dateOfBirth,
            String allergies,
            String chronicDiseases,
            String specialNeeds,
            String emergencyContact
    ) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.allergies = allergies;
        this.chronicDiseases = chronicDiseases;
        this.specialNeeds = specialNeeds;
        this.emergencyContact = emergencyContact;
    }

    public ChildEntity(
            String name,
            LocalDate dateOfBirth,
            String allergies,
            String chronicDiseases,
            String specialNeeds,
            String emergencyContact,
            String groupName,
            String parentName,
            String parentEmail,
            String healthNotes
    ) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.allergies = allergies;
        this.chronicDiseases = chronicDiseases;
        this.specialNeeds = specialNeeds;
        this.emergencyContact = emergencyContact;
        this.groupName = groupName;
        this.parentName = parentName;
        this.parentEmail = parentEmail;
        this.healthNotes = healthNotes;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getChronicDiseases() { return chronicDiseases; }
    public void setChronicDiseases(String chronicDiseases) { this.chronicDiseases = chronicDiseases; }

    public String getSpecialNeeds() { return specialNeeds; }
    public void setSpecialNeeds(String specialNeeds) { this.specialNeeds = specialNeeds; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getParentEmail() { return parentEmail; }
    public void setParentEmail(String parentEmail) { this.parentEmail = parentEmail; }

    public String getHealthNotes() { return healthNotes; }
    public void setHealthNotes(String healthNotes) { this.healthNotes = healthNotes; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
