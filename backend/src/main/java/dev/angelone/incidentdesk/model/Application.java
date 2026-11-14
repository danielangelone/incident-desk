package dev.angelone.incidentdesk.model;

import jakarta.persistence.*;

@Entity
@Table(name = "application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "owner_team", nullable = false)
    private String ownerTeam;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getOwnerTeam() { return ownerTeam; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setOwnerTeam(String ownerTeam) { this.ownerTeam = ownerTeam; }
}
