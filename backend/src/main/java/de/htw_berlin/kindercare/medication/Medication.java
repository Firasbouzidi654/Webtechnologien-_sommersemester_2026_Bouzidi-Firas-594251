package de.htw_berlin.kindercare.medication;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "medications")
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String childName;
    private String dosage;
    @Column(name = "medication_time")
    private String time;
    private String status;

    public Medication() { }

    public Medication(String name, String childName, String dosage) {
        this(name, childName, dosage, "12:00", "PENDING");
    }

    public Medication(String name, String childName, String dosage, String time, String status) {
        this.name = name;
        this.childName = childName;
        this.dosage = dosage;
        this.time = time;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
