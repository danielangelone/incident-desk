package dev.angelone.incidentdesk.repository;

import dev.angelone.incidentdesk.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepository extends JpaRepository<Server, Long> {}
