package de.htw_berlin.KinderCareConnect.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parent_notes")
public class ParentNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long childId;

    @Column(columnDefinition = "TEXT")
    private String note;

    public ParentNoteEntity() {
    }

    public ParentNoteEntity(Long childId, String note) {
        this.childId = childId;
        this.note = note;
    }

    public Long getId() { return id; }

    public Long getChildId() { return childId; }
    public void setChildId(Long childId) { this.childId = childId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
