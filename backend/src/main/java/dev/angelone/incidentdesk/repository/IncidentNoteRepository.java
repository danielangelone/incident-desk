package dev.angelone.incidentdesk.repository;

import dev.angelone.incidentdesk.model.IncidentNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentNoteRepository extends JpaRepository<IncidentNote, Long> {}
