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
    private Long childId;
    private String dosage;
    @Column(name = "medication_time")
    private String time;
    private String status;
    @Column(name = "scheduled_date")
    private String scheduledDate;

    public Medication() { }

    public Medication(String name, String childName, String dosage) {
        this(name, childName, dosage, "12:00", "PENDING", null);
    }

    public Medication(String name, String childName, String dosage, String time, String status, String scheduledDate) {
        this.name = name;
        this.childName = childName;
        this.dosage = dosage;
        this.time = time;
        this.status = status;
        this.scheduledDate = scheduledDate;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }
    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }
}
