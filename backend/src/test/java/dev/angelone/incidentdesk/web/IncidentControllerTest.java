package dev.angelone.incidentdesk.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.angelone.incidentdesk.model.*;
import dev.angelone.incidentdesk.service.IncidentService;
import dev.angelone.incidentdesk.service.InvalidTransitionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean IncidentService service;

    private Incident sampleIncident() {
        Application app = new Application();
        app.setId(1L);
        app.setName("payments-api");
        app.setOwnerTeam("payments");

        Incident i = new Incident();
        i.setId(1L);
        i.setTitle("test incident");
        i.setSeverity(Severity.HIGH);
        i.setStatus(Status.OPEN);
        i.setApplication(app);
        return i;
    }

    @Test
    void createIncident() throws Exception {
        when(service.create(eq("test incident"), eq(Severity.HIGH), eq(1L), isNull()))
                .thenReturn(sampleIncident());
        when(service.get(1L)).thenReturn(sampleIncident());

        mvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title":"test incident","severity":"HIGH","applicationId":1}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createIncidentWithMissingTitleReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"severity":"HIGH","applicationId":1}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void resolveWithoutRootCauseReturns422() throws Exception {
        when(service.updateStatus(eq(1L), eq(Status.RESOLVED), isNull()))
                .thenThrow(new InvalidTransitionException("rootCause is required"));

        mvc.perform(patch("/api/incidents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"status":"RESOLVED"}
                            """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("rootCause is required"));
    }
}
