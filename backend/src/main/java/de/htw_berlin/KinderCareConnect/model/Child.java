package de.htw_berlin.KinderCareConnect.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Entity class representing a child in the KinderCare Connect system.
 * Contains health-related information about the child.
 */

@Entity
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String dateOfBirth;
    private String allergies;
    private String chronicDiseases;
    private String specialNeeds;
    private String emergencyContact;

    // Constructor
    public Child() {}

    public Child(Long id, String name, String dateOfBirth, String allergies, 
                 String chronicDiseases, String specialNeeds, String emergencyContact) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.allergies = allergies;
        this.chronicDiseases = chronicDiseases;
        this.specialNeeds = specialNeeds;
        this.emergencyContact = emergencyContact;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getChronicDiseases() { return chronicDiseases; }
    public void setChronicDiseases(String chronicDiseases) { this.chronicDiseases = chronicDiseases; }

    public String getSpecialNeeds() { return specialNeeds; }
    public void setSpecialNeeds(String specialNeeds) { this.specialNeeds = specialNeeds; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    @Override
    public String toString() {
        return "Child{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", allergies='" + allergies + '\'' +
                ", chronicDiseases='" + chronicDiseases + '\'' +
                ", specialNeeds='" + specialNeeds + '\'' +
                ", emergencyContact='" + emergencyContact + '\'' +
                '}';
    }
}

