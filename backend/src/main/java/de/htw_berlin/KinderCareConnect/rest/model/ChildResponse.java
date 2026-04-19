package de.htw_berlin.KinderCareConnect.rest.model;

public class ChildResponse {
    private final Long id;
    private final String name;
    private final String dateOfBirth;
    private final String allergies;
    private final String chronicDiseases;
    private final String specialNeeds;
    private final String emergencyContact;

    public ChildResponse(
        Long id,
        String name,
        String dateOfBirth,
        String allergies,
        String chronicDiseases,
        String specialNeeds,
        String emergencyContact
    ) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.allergies = allergies;
        this.chronicDiseases = chronicDiseases;
        this.specialNeeds = specialNeeds;
        this.emergencyContact = emergencyContact;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAllergies() {
        return allergies;
    }

    public String getChronicDiseases() {
        return chronicDiseases;
    }

    public String getSpecialNeeds() {
        return specialNeeds;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }
}
