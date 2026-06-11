package de.htw_berlin.KinderCareConnect.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.lang.NonNull;

import java.util.Objects;

@Entity
@Table(name = "medication_logs")
public class MedicationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String medicationId;
    private Long childId;
    private String status;
    private String adminName;
    private String loggedAt;
    private String note;

    @NonNull
    public Long getId() { return id; }

    public String getMedicationId() { return medicationId; }
    public void setMedicationId(String medicationId) { this.medicationId = medicationId; }

    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public String getLoggedAt() { return loggedAt; }
    public void setLoggedAt(String loggedAt) { this.loggedAt = loggedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
