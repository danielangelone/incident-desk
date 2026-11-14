package dev.angelone.incidentdesk.repository;

import dev.angelone.incidentdesk.model.Incident;
import dev.angelone.incidentdesk.model.Severity;
import dev.angelone.incidentdesk.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @Query("""
        select i from Incident i
        where (:status is null or i.status = :status)
          and (:severity is null or i.severity = :severity)
          and (:applicationId is null or i.application.id = :applicationId)
        order by i.startedAt desc
        """)
    Page<Incident> findFiltered(
            @Param("status") Status status,
            @Param("severity") Severity severity,
            @Param("applicationId") Long applicationId,
            Pageable pageable);

    @Query("select count(i) from Incident i where i.status != 'RESOLVED'")
    long countOpen();

    @Query("select i.severity, count(i) from Incident i group by i.severity")
    java.util.List<Object[]> countBySeverity();

    // mean hours from startedAt to resolvedAt for incidents resolved in the last 90 days
    @Query("""
        select avg(extract(epoch from (i.resolvedAt - i.startedAt)) / 3600.0)
        from Incident i
        where i.resolvedAt is not null
          and i.resolvedAt > :since
        """)
    Optional<Double> meanTimeToResolveHours(@Param("since") Instant since);

    @Query("select i from Incident i join fetch i.application left join fetch i.server where i.id = :id")
    Optional<Incident> findByIdWithRelations(@Param("id") Long id);
}
