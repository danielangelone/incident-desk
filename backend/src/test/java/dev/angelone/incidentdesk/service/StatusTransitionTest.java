package dev.angelone.incidentdesk.service;

import dev.angelone.incidentdesk.model.*;
import dev.angelone.incidentdesk.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusTransitionTest {

    @Mock IncidentRepository incidents;
    @Mock IncidentNoteRepository notes;
    @Mock ApplicationRepository applications;
    @Mock ServerRepository servers;

    @InjectMocks IncidentService service;

    private Incident incidentWith(Status status) {
        Application app = new Application();
        app.setId(1L);
        app.setName("test-app");
        app.setOwnerTeam("team");

        Incident i = new Incident();
        i.setId(1L);
        i.setTitle("test");
        i.setSeverity(Severity.LOW);
        i.setStatus(status);
        i.setApplication(app);
        return i;
    }

    @Test
    void openCanMoveToInvestigating() {
        Incident i = incidentWith(Status.OPEN);
        when(incidents.findByIdWithRelations(1L)).thenReturn(Optional.of(i));
        when(incidents.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incident result = service.updateStatus(1L, Status.INVESTIGATING, null);
        assertThat(result.getStatus()).isEqualTo(Status.INVESTIGATING);
    }

    @Test
    void openCanResolveWithRootCause() {
        Incident i = incidentWith(Status.OPEN);
        when(incidents.findByIdWithRelations(1L)).thenReturn(Optional.of(i));
        when(incidents.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Incident result = service.updateStatus(1L, Status.RESOLVED, "found it");
        assertThat(result.getStatus()).isEqualTo(Status.RESOLVED);
        assertThat(result.getResolvedAt()).isNotNull();
    }

    @Test
    void resolvingWithoutRootCauseIsRejected() {
        Incident i = incidentWith(Status.INVESTIGATING);
        when(incidents.findByIdWithRelations(1L)).thenReturn(Optional.of(i));

        assertThatThrownBy(() -> service.updateStatus(1L, Status.RESOLVED, null))
                .isInstanceOf(InvalidTransitionException.class)
                .hasMessageContaining("rootCause");
    }

    @Test
    void resolvedIncidentCannotBeReopened() {
        Incident i = incidentWith(Status.RESOLVED);
        when(incidents.findByIdWithRelations(1L)).thenReturn(Optional.of(i));

        assertThatThrownBy(() -> service.updateStatus(1L, Status.OPEN, null))
                .isInstanceOf(InvalidTransitionException.class)
                .hasMessageContaining("resolved");
    }

    @Test
    void investigatingCannotGoBackToOpen() {
        Incident i = incidentWith(Status.INVESTIGATING);
        when(incidents.findByIdWithRelations(1L)).thenReturn(Optional.of(i));

        assertThatThrownBy(() -> service.updateStatus(1L, Status.OPEN, null))
                .isInstanceOf(InvalidTransitionException.class);
    }
}
