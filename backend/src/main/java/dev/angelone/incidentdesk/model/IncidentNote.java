package dev.angelone.incidentdesk.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "incident_note")
public class IncidentNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Incident getIncident() { return incident; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setIncident(Incident incident) { this.incident = incident; }
    public void setBody(String body) { this.body = body; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
