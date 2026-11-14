package dev.angelone.incidentdesk.web;

import dev.angelone.incidentdesk.model.Server;
import dev.angelone.incidentdesk.repository.ServerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerRepository repo;

    public ServerController(ServerRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<ServerResponse> list() {
        return repo.findAll().stream().map(ServerResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServerResponse create(@Valid @RequestBody ServerRequest req) {
        Server server = new Server();
        server.setHostname(req.hostname());
        server.setEnvironment(req.environment());
        return ServerResponse.from(repo.save(server));
    }

    record ServerRequest(@NotBlank String hostname, @NotNull Server.Environment environment) {}

    record ServerResponse(Long id, String hostname, Server.Environment environment) {
        static ServerResponse from(Server s) {
            return new ServerResponse(s.getId(), s.getHostname(), s.getEnvironment());
        }
    }
}
