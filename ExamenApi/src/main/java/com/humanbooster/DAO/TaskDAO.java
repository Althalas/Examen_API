package com.humanbooster.DAO;

import com.humanbooster.model.Task;
import com.humanbooster.util.H2DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDAO {

    // Helper method to map a ResultSet row to a Task object
    private Task mapRowToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setDone(rs.getBoolean("done"));
        // Convertir SQL Timestamp en LocalDateTime
        Timestamp createdAtTimestamp = rs.getTimestamp("createdAt");
        if (createdAtTimestamp != null) {
            task.setCreatedAt(createdAtTimestamp.toLocalDateTime());
        }
        Timestamp updatedAtTimestamp = rs.getTimestamp("updatedAt");
        if (updatedAtTimestamp != null) {
            task.setUpdatedAt(updatedAtTimestamp.toLocalDateTime());
        }
        return task;
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (Connection connection = H2DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de toutes les tâches: " + e.getMessage());
        }
        return tasks;
    }

    public Optional<Task> getTaskById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection connection = H2DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la tâche par ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public Task createTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, done, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();

        try (Connection connection = H2DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setBoolean(3, task.isDone());
            ps.setTimestamp(4, Timestamp.valueOf(now)); // Convertir LocalDateTime en SQL Timestamp
            ps.setTimestamp(5, Timestamp.valueOf(now));

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création de la tâche a échoué, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getLong(1));
                    task.setCreatedAt(now);
                    task.setUpdatedAt(now);
                    return task;
                } else {
                    throw new SQLException("La création de la tâche a échoué, aucun ID obtenu.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la tâche: " + e.getMessage());
            // Gérer l'exception de manière appropriée
            return null; // Ou lancer une exception
        }
    }

    public Optional<Task> updateTask(Long id, Task taskDetails) {
        String sql = "UPDATE tasks SET title = ?, description = ?, done = ?, updatedAt = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();

        try (Connection connection = H2DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, taskDetails.getTitle());
            ps.setString(2, taskDetails.getDescription());
            ps.setBoolean(3, taskDetails.isDone());
            ps.setTimestamp(4, Timestamp.valueOf(now));
            ps.setLong(5, id);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                taskDetails.setId(id); // Assurer que l'ID est correct
                taskDetails.setUpdatedAt(now);
                // Pour avoir createdAt, il faudrait le relire ou le passer
                // Ici, on va juste retourner taskDetails avec updatedAt mis à jour.
                // Pour une version complète, on pourrait relire la tâche depuis la BDD.
                return getTaskById(id); // Relire pour avoir toutes les infos à jour, y compris createdAt
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la tâche ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean deleteTask(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection connection = H2DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la tâche ID " + id + ": " + e.getMessage());
        }
        return false;
    }
}
