package dev.angelone.incidentdesk.service;

import dev.angelone.incidentdesk.model.*;
import dev.angelone.incidentdesk.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Map;

@Service
@Transactional
public class IncidentService {

    private final IncidentRepository incidents;
    private final IncidentNoteRepository notes;
    private final ApplicationRepository applications;
    private final ServerRepository servers;

    public IncidentService(IncidentRepository incidents, IncidentNoteRepository notes,
                           ApplicationRepository applications, ServerRepository servers) {
        this.incidents = incidents;
        this.notes = notes;
        this.applications = applications;
        this.servers = servers;
    }

    @Transactional(readOnly = true)
    public Page<Incident> list(Status status, Severity severity, Long applicationId, int page, int size) {
        return incidents.findFiltered(status, severity, applicationId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Incident get(Long id) {
        return incidents.findByIdWithRelations(id)
                .orElseThrow(() -> new NotFoundException("incident " + id + " not found"));
    }

    public Incident create(String title, Severity severity, Long applicationId, Long serverId) {
        Application app = applications.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("application " + applicationId + " not found"));

        Server server = null;
        if (serverId != null) {
            server = servers.findById(serverId)
                    .orElseThrow(() -> new NotFoundException("server " + serverId + " not found"));
        }

        Incident incident = new Incident();
        incident.setTitle(title);
        incident.setSeverity(severity);
        incident.setApplication(app);
        incident.setServer(server);

        return incidents.save(incident);
    }

    public Incident updateStatus(Long id, Status newStatus, String rootCause) {
        Incident incident = incidents.findByIdWithRelations(id)
                .orElseThrow(() -> new NotFoundException("incident " + id + " not found"));

        validateTransition(incident.getStatus(), newStatus);

        if (newStatus == Status.RESOLVED) {
            if (rootCause == null || rootCause.isBlank()) {
                throw new InvalidTransitionException("rootCause is required when resolving an incident");
            }
            incident.setRootCause(rootCause);
            incident.setResolvedAt(Instant.now());
        }

        incident.setStatus(newStatus);
        return incidents.save(incident);
    }

    // Allowed transitions: OPEN -> INVESTIGATING, OPEN -> RESOLVED, INVESTIGATING -> RESOLVED.
    // A resolved incident cannot be reopened.
    private void validateTransition(Status current, Status next) {
        if (current == Status.RESOLVED) {
            throw new InvalidTransitionException("resolved incidents cannot be reopened");
        }
        if (current == Status.INVESTIGATING && next == Status.OPEN) {
            throw new InvalidTransitionException("cannot move from INVESTIGATING back to OPEN");
        }
    }

    public IncidentNote addNote(Long incidentId, String body) {
        Incident incident = incidents.findById(incidentId)
                .orElseThrow(() -> new NotFoundException("incident " + incidentId + " not found"));

        IncidentNote note = new IncidentNote();
        note.setIncident(incident);
        note.setBody(body);

        return notes.save(note);
    }

    @Transactional(readOnly = true)
    public Stats stats() {
        long openCount = incidents.countOpen();

        Map<Severity, Long> bySeverity = new EnumMap<>(Severity.class);
        for (Object[] row : incidents.countBySeverity()) {
            bySeverity.put((Severity) row[0], (Long) row[1]);
        }

        Instant since = Instant.now().minus(90, ChronoUnit.DAYS);
        Double mttr = incidents.meanTimeToResolveHours(since).orElse(null);

        return new Stats(openCount, bySeverity, mttr);
    }

    public record Stats(long openCount, Map<Severity, Long> bySeverity, Double meanTimeToResolveHours) {}
}
