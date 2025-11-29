package com.cbp.TaskManager;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    public List<Task> getAll() {
        return repo.findAll();
    }

    public Task getById(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Task not found with id " + id));
    }

    public Task create(Task task) {
        task.setId(null); // let DB generate id
        return repo.save(task);
    }

    public Task update(Long id, Task incoming) {
        Task t = getById(id);
        if (incoming.getTitle() != null) t.setTitle(incoming.getTitle());
        if (incoming.getDescription() != null) t.setDescription(incoming.getDescription());
        if (incoming.getStatus() != null) t.setStatus(incoming.getStatus());
        return repo.save(t);
    }

    public void delete(Long id) {
        Task t = getById(id);
        repo.delete(t);
    }
}
