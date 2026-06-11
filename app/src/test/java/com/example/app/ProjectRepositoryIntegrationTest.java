package com.example.app;

import com.example.app.data.Project;
import com.example.app.data.User;
import com.example.app.repository.ProjectRepository;
import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProjectRepositoryIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String username, String email) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setFirstName("Jan");
        u.setLastName("Test");
        u.setPassword("hashed");
        u.setRole("ROLE_USER");
        return u;
    }

    @Test
    void shouldSaveProjectAndFindById() {
        Project p = new Project();
        p.setName("Nowy projekt");

        Project saved = projectRepository.save(p);

        Optional<Project> found = projectRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Nowy projekt", found.get().getName());
    }

    @Test
    void shouldFindProjectByName() {
        Project p = new Project();
        p.setName("Unikatowy Projekt");
        projectRepository.save(p);

        Optional<Project> found = projectRepository.findByName("Unikatowy Projekt");

        assertTrue(found.isPresent());
        assertEquals("Unikatowy Projekt", found.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenProjectNameNotFound() {
        Optional<Project> found = projectRepository.findByName("Nieistniejacy");

        assertFalse(found.isPresent());
    }

    @Test
    void shouldReturnTrueWhenProjectNameExists() {
        Project p = new Project();
        p.setName("Istniejacy");
        projectRepository.save(p);

        assertTrue(projectRepository.existsByName("Istniejacy"));
    }

    @Test
    void shouldReturnFalseWhenProjectNameNotExists() {
        assertFalse(projectRepository.existsByName("Nieistniejacy99"));
    }

    @Test
    void shouldFindProjectsByNameContainingIgnoreCase() {
        Project p1 = new Project(); p1.setName("Mobile App");
        Project p2 = new Project(); p2.setName("Mobile Backend");
        Project p3 = new Project(); p3.setName("Web Portal");
        projectRepository.saveAll(List.of(p1, p2, p3));

        Page<Project> result = projectRepository.findByNameContainingIgnoreCase(
                "mobile", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(p -> p.getName().toLowerCase().contains("mobile")));
    }

    @Test
    void shouldFindProjectsByNameIgnoringCase() {
        Project p = new Project();
        p.setName("UPPERCASE PROJECT");
        projectRepository.save(p);

        Page<Project> result = projectRepository.findByNameContainingIgnoreCase(
                "uppercase", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldReturnAllProjectsOnFindAll() {
        projectRepository.save(buildProject("Projekt 1"));
        projectRepository.save(buildProject("Projekt 2"));
        projectRepository.save(buildProject("Projekt 3"));

        List<Project> all = projectRepository.findAll();

        assertEquals(3, all.size());
    }

    @Test
    void shouldDeleteProject() {
        Project p = projectRepository.save(buildProject("Do usuniecia"));
        Long id = p.getId();

        projectRepository.deleteById(id);

        assertFalse(projectRepository.findById(id).isPresent());
    }

    @Test
    void shouldUpdateProjectName() {
        Project p = projectRepository.save(buildProject("Stara Nazwa"));

        p.setName("Nowa Nazwa");
        projectRepository.save(p);

        Project updated = projectRepository.findById(p.getId()).orElseThrow();
        assertEquals("Nowa Nazwa", updated.getName());
    }

    @Test
    void shouldAssociateUserWithProject() {
        User user = userRepository.save(buildUser("teamuser", "team@test.com"));

        Project project = new Project();
        project.setName("Team Project");
        project.getUsers().add(user);
        Project saved = projectRepository.save(project);

        Project found = projectRepository.findById(saved.getId()).orElseThrow();
        assertFalse(found.getUsers().isEmpty());
    }

    @Test
    void shouldPaginateProjectResults() {
        for (int i = 1; i <= 15; i++) {
            projectRepository.save(buildProject("Projekt " + i));
        }

        Page<Project> firstPage = projectRepository.findAll(PageRequest.of(0, 5));
        Page<Project> secondPage = projectRepository.findAll(PageRequest.of(1, 5));

        assertEquals(5, firstPage.getContent().size());
        assertEquals(5, secondPage.getContent().size());
        assertEquals(15, firstPage.getTotalElements());
        assertEquals(3, firstPage.getTotalPages());
    }

    private Project buildProject(String name) {
        Project p = new Project();
        p.setName(name);
        return p;
    }
}