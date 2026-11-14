package dev.angelone.incidentdesk.web;

import dev.angelone.incidentdesk.model.*;
import dev.angelone.incidentdesk.service.IncidentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService service;

    public IncidentController(IncidentService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<IncidentSummary> list(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) Long applicationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Incident> result = service.list(status, severity, applicationId, page, size);
        return PageResponse.of(result, result.getContent().stream().map(IncidentSummary::from).toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentDetail create(@Valid @RequestBody CreateRequest req) {
        Incident incident = service.create(req.title(), req.severity(), req.applicationId(), req.serverId());
        return IncidentDetail.from(service.get(incident.getId()));
    }

    @GetMapping("/{id}")
    public IncidentDetail get(@PathVariable Long id) {
        return IncidentDetail.from(service.get(id));
    }

    @PatchMapping("/{id}/status")
    public IncidentDetail updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return IncidentDetail.from(service.updateStatus(id, req.status(), req.rootCause()));
    }

    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse addNote(@PathVariable Long id, @Valid @RequestBody NoteRequest req) {
        IncidentNote note = service.addNote(id, req.body());
        return NoteResponse.from(note);
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        IncidentService.Stats s = service.stats();
        return new StatsResponse(s.openCount(), s.bySeverity(), s.meanTimeToResolveHours());
    }

    // --- request records ---

    record CreateRequest(
            @NotBlank String title,
            @NotNull Severity severity,
            @NotNull Long applicationId,
            Long serverId) {}

    record StatusRequest(@NotNull Status status, String rootCause) {}

    record NoteRequest(@NotBlank String body) {}

    // --- response records ---

    record IncidentSummary(Long id, String title, Severity severity, Status status,
                           Long applicationId, String applicationName,
                           Long serverId, String serverHostname,
                           Instant startedAt, Instant resolvedAt) {
        static IncidentSummary from(Incident i) {
            return new IncidentSummary(
                    i.getId(), i.getTitle(), i.getSeverity(), i.getStatus(),
                    i.getApplication().getId(), i.getApplication().getName(),
                    i.getServer() != null ? i.getServer().getId() : null,
                    i.getServer() != null ? i.getServer().getHostname() : null,
                    i.getStartedAt(), i.getResolvedAt());
        }
    }

    record IncidentDetail(Long id, String title, Severity severity, Status status,
                          Long applicationId, String applicationName,
                          Long serverId, String serverHostname,
                          Instant startedAt, Instant resolvedAt, String rootCause,
                          List<NoteResponse> notes) {
        static IncidentDetail from(Incident i) {
            return new IncidentDetail(
                    i.getId(), i.getTitle(), i.getSeverity(), i.getStatus(),
                    i.getApplication().getId(), i.getApplication().getName(),
                    i.getServer() != null ? i.getServer().getId() : null,
                    i.getServer() != null ? i.getServer().getHostname() : null,
                    i.getStartedAt(), i.getResolvedAt(), i.getRootCause(),
                    i.getNotes().stream().map(NoteResponse::from).toList());
        }
    }

    record NoteResponse(Long id, String body, Instant createdAt) {
        static NoteResponse from(IncidentNote n) {
            return new NoteResponse(n.getId(), n.getBody(), n.getCreatedAt());
        }
    }

    record StatsResponse(long openCount, Map<Severity, Long> bySeverity, Double meanTimeToResolveHours) {}

    record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        static <T> PageResponse<T> of(Page<?> p, List<T> content) {
            return new PageResponse<>(content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        }
    }
}
