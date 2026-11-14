package dev.angelone.incidentdesk.web;

import dev.angelone.incidentdesk.model.Application;
import dev.angelone.incidentdesk.repository.ApplicationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationRepository repo;

    public ApplicationController(ApplicationRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<ApplicationResponse> list() {
        return repo.findAll().stream().map(ApplicationResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @RequestBody ApplicationRequest req) {
        Application app = new Application();
        app.setName(req.name());
        app.setOwnerTeam(req.ownerTeam());
        return ApplicationResponse.from(repo.save(app));
    }

    record ApplicationRequest(@NotBlank String name, @NotBlank String ownerTeam) {}

    record ApplicationResponse(Long id, String name, String ownerTeam) {
        static ApplicationResponse from(Application a) {
            return new ApplicationResponse(a.getId(), a.getName(), a.getOwnerTeam());
        }
    }
}
