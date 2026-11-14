package dev.angelone.incidentdesk.model;

import jakarta.persistence.*;

@Entity
@Table(name = "server")
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String hostname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Environment environment;

    public enum Environment { PROD, STAGING }

    public Long getId() { return id; }
    public String getHostname() { return hostname; }
    public Environment getEnvironment() { return environment; }

    public void setId(Long id) { this.id = id; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public void setEnvironment(Environment environment) { this.environment = environment; }
}
