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
    private String frequency;
    private Integer intervalDays;
    private String dayOfWeek;
    @Column(name = "start_date")
    private String startDate;

    public Medication() { }

    public Medication(String name, String childName, String dosage) {
        this(name, childName, dosage, "12:00", "PENDING", "DAILY", null, null);
    }

    public Medication(String name, String childName, String dosage, String time, String status,
                      String frequency, Integer intervalDays, String startDate) {
        this.name = name;
        this.childName = childName;
        this.dosage = dosage;
        this.time = time;
        this.status = status;
        this.frequency = frequency;
        this.intervalDays = intervalDays;
        this.startDate = startDate;
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
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public Integer getIntervalDays() { return intervalDays; }
    public void setIntervalDays(Integer intervalDays) { this.intervalDays = intervalDays; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
}
