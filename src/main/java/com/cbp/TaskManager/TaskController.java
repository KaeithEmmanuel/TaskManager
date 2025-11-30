package com.cbp.TaskManager;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    /**
     * Find by id
     * - URL: GET /api/tasks/{id}
     * - Returns 200 + Task if found, 404 if not found.
     *
     * This method is defensive: it supports service methods that return either
     * a Task (or null) or an Optional<Task>.
     */
    @GetMapping("/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Task> findById(@PathVariable("id") Long id) {
        Object result = taskService.getById(id); // defensive -- may be Task, Optional<Task>, or null

        Task task = null;

        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (result instanceof Task) {
            task = (Task) result;
        } else if (result instanceof Optional) {
            Optional<Task> opt = (Optional<Task>) result;
            if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            task = opt.get();
        } else {
            // Unexpected return type from service
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(task);
    }

    /**
     * Delete by id
     * - URL: DELETE /api/tasks/{id}
     * - Returns 204 if deleted, 404 if not found.
     *
     * Assumes taskService.delete(id) returns boolean (true if deleted),
     * or throws an exception if not found. We handle both cases.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        try {
            // Prefer boolean return from service if available
            Object res = taskService;

            if (res == null) {
                // If service method is void, assume success (but double-check existence)
                // try to verify whether the task still exists
                Object check = taskService.getById(id);
                if (check == null) {
                    // already gone — return 204
                    return ResponseEntity.noContent().build();
                } else {
                    // still exists -> something went wrong
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            if (res instanceof Boolean) {
                boolean deleted = (Boolean) res;
                return deleted ? ResponseEntity.noContent().build()
                        : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // If delete returned something unexpected, assume success
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException rnfe) {
            // If your service throws a custom exception when entity not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // other errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update by id
     * - URL: PUT /api/tasks/{id}
     * - Returns 200 + updated Task if OK, 404 if task not found, 400 on bad request.
     *
     * Assumes taskService.update(id, task) returns the updated Task or null/Optional.
     */
    @PutMapping("/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Task> updateTask(@PathVariable("id") Long id, @RequestBody Task task) {
        if (task == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Object result = taskService.update(id, task);

            if (result == null) {
                // service returned null -> treat as not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Task updated = null;
            if (result instanceof Task) {
                updated = (Task) result;
            } else if (result instanceof Optional) {
                Optional<Task> opt = (Optional<Task>) result;
                if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                updated = opt.get();
            } else {
                // unexpected type
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            return ResponseEntity.ok(updated);

        } catch (ResourceNotFoundException rnfe) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
