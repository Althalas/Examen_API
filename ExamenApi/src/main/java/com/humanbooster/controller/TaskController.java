package com.humanbooster.controller;

import com.humanbooster.DAO.TaskDAO;
import com.humanbooster.model.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller pour les API
 * GET /api/tasks -> List all tasks
 * GET /api/tasks/{id} -> Get a specific task
 * POST /api/tasks -> Create a new task
 * PUT /api/tasks/{id} -> Update an existing task
 * DELETE /api/tasks/{id} -> Delete a task
 */
public class TaskController extends HttpServlet {

    private final TaskDAO taskDAO = new TaskDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Pattern to extract ID from paths like /tasks/123
    // It captures one or more digits at the end of the string, assuming pathInfo starts with /tasks/
    private final Pattern pathPattern = Pattern.compile("^/tasks/(\\d+)$"); // Ajout de ^ pour s'assurer qu'il commence par /tasks/

    @Override
    public void init() throws ServletException {
        super.init();
        // Register JavaTimeModule to handle LocalDateTime serialization/deserialization
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Handles GET requests.
     * If the path is /tasks, lists all tasks.
     * If the path is /tasks/{id}, retrieves a specific task.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo(); // e.g., "/tasks" or "/tasks/123" when servlet is mapped to /api/*

        // Route for GET /api/tasks (list all tasks)
        if ("/tasks".equals(pathInfo)) {
            List<Task> tasks = taskDAO.getAllTasks();
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
            objectMapper.writeValue(resp.getOutputStream(), tasks);
        } else {
            // Route for GET /api/tasks/{id} (get specific task)
            Matcher matcher = pathPattern.matcher(pathInfo != null ? pathInfo : "");
            if (matcher.matches()) {
                try {
                    Long id = Long.parseLong(matcher.group(1));
                    Optional<Task> taskOptional = taskDAO.getTaskById(id);

                    if (taskOptional.isPresent()) {
                        resp.setContentType("application/json");
                        resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
                        objectMapper.writeValue(resp.getOutputStream(), taskOptional.get());
                    } else {
                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Task not found with id: " + id); // 404 Not Found
                    }
                } catch (NumberFormatException e) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format: " + matcher.group(1)); // 400 Bad Request
                }
            } else {
                // If pathInfo is not "/tasks" and does not match "/tasks/{id}"
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid GET path: " + (pathInfo != null ? pathInfo : "null"));
            }
        }
    }

    /**
     * Handles POST requests to /api/tasks.
     * Creates a new task.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        // Only allow POST to /api/tasks
        if ("/tasks".equals(pathInfo)) {
            try {
                Task taskToCreate = objectMapper.readValue(req.getInputStream(), Task.class);

                if (taskToCreate.getTitle() == null || taskToCreate.getTitle().trim().isEmpty()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Task title cannot be empty or missing.");
                    return;
                }

                Task createdTask = taskDAO.createTask(taskToCreate);
                resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
                objectMapper.writeValue(resp.getOutputStream(), createdTask);

            } catch (IOException e) { // Catch specific Jackson parsing or I/O errors
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format or request data: " + e.getMessage());
            } catch (Exception e) { // Catch any other unexpected errors during task creation
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating task: " + e.getMessage());
            }
        } else {
            sendError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST method not allowed for path: " + (pathInfo != null ? pathInfo : "null"));
        }
    }

    /**
     * Handles PUT requests to /api/tasks/{id}.
     * Updates an existing task.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        Matcher matcher = pathPattern.matcher(pathInfo != null ? pathInfo : "");

        if (matcher.matches()) { // Corresponds to /api/tasks/{id}
            try {
                Long id = Long.parseLong(matcher.group(1));
                Task taskUpdates = objectMapper.readValue(req.getInputStream(), Task.class);

                if (taskUpdates.getTitle() == null || taskUpdates.getTitle().trim().isEmpty()) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Task title cannot be empty or missing for update.");
                    return;
                }

                Optional<Task> updatedTaskOptional = taskDAO.updateTask(id, taskUpdates);

                if (updatedTaskOptional.isPresent()) {
                    resp.setContentType("application/json");
                    resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
                    objectMapper.writeValue(resp.getOutputStream(), updatedTaskOptional.get());
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Task not found with id: " + id + " for update."); // 404 Not Found
                }
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format: " + matcher.group(1));
            } catch (IOException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format or request data for update: " + e.getMessage());
            } catch (Exception e) {
                sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating task: " + e.getMessage());
            }
        } else {
            sendError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "PUT method not allowed or invalid path: " + (pathInfo != null ? pathInfo : "null"));
        }
    }

    /**
     * Handles DELETE requests to /api/tasks/{id}.
     * Deletes a specific task.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        Matcher matcher = pathPattern.matcher(pathInfo != null ? pathInfo : "");

        if (matcher.matches()) { // Corresponds to /api/tasks/{id}
            try {
                Long id = Long.parseLong(matcher.group(1));
                boolean deleted = taskDAO.deleteTask(id);

                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 No Content
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Task not found with id: " + id + " for deletion."); // 404 Not Found
                }
            } catch (NumberFormatException e) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID format: " + matcher.group(1));
            }
        } else {
            sendError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "DELETE method not allowed or invalid path: " + (pathInfo != null ? pathInfo : "null"));
        }
    }

    /**
     * Helper method to send an error response with a JSON body.
     * @param resp The HttpServletResponse object.
     * @param statusCode The HTTP status code.
     * @param message The error message.
     * @throws IOException If an I/O error occurs.
     */
    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8"); // Assurer l'encodage correct des caractères
        resp.setStatus(statusCode);
        // Simple JSON error response: {"error": "message"}
        objectMapper.writeValue(resp.getOutputStream(), Map.of("error", message));
    }
}
