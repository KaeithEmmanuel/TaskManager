package com.cbp.TaskManager;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/addOne")
    public ResponseEntity<Task> addOneTask(@RequestBody Task task) {
        Task created = taskService.create(task);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<Task>> findAllTasks() {
        List<Task> taskList = taskService.getAll();
        return new ResponseEntity<>(taskList, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<Task> findById(@PathVariable("id") Long id) {
        Task task = taskService.getById(id);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        taskService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("{id}")
    public ResponseEntity<Task> updateTask(@PathVariable("id") Long id, @RequestBody Task task) {
        Task updated = taskService.update(id, task);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }
}
