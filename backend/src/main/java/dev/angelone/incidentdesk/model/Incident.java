package dev.angelone.incidentdesk.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incident")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "root_cause")
    private String rootCause;

    @OneToMany(mappedBy = "incident", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("createdAt asc")
    private List<IncidentNote> notes = new ArrayList<>();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Severity getSeverity() { return severity; }
    public Status getStatus() { return status; }
    public Application getApplication() { return application; }
    public Server getServer() { return server; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public String getRootCause() { return rootCause; }
    public List<IncidentNote> getNotes() { return notes; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setStatus(Status status) { this.status = status; }
    public void setApplication(Application application) { this.application = application; }
    public void setServer(Server server) { this.server = server; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
}
