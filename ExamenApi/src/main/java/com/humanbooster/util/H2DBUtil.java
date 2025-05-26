package com.humanbooster.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class H2DBUtil {

    // JDBC URL pour une base de données H2 en mémoire nommée 'taskdb'
    // DB_CLOSE_DELAY=-1 permet de garder la base de données en mémoire tant que la JVM est active.
    private static final String JDBC_URL = "jdbc:h2:mem:taskdb;DB_CLOSE_DELAY=-1";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";

    // Méthode pour obtenir une connexion à la base de données
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    // Méthode pour initialiser le schéma de la base de données (créer la table des tâches)
    public static void initializeDatabase() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // SQL pour créer la table 'tasks' si elle n'existe pas déjà
            // L'ID est auto-incrémenté
            // createdAt et updatedAt utiliseront TIMESTAMP pour stocker LocalDateTime
            String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "done BOOLEAN DEFAULT FALSE, " +
                    "createdAt TIMESTAMP, " +
                    "updatedAt TIMESTAMP" +
                    ")";
            statement.execute(createTableSQL);
            System.out.println("Table 'tasks' initialisée avec succès ou déjà existante.");

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données H2:");
            e.printStackTrace();
            // Dans une application réelle, vous pourriez vouloir gérer cette erreur de manière plus robuste
            // (par exemple, arrêter l'application si la BDD ne peut pas être initialisée)
            throw new RuntimeException("Impossible d'initialiser la base de données", e);
        }
    }

    // Optionnel: Méthode pour charger le driver H2 explicitement,
    // bien que cela soit généralement géré automatiquement avec JDBC 4.0+
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver H2 non trouvé. Assurez-vous que la dépendance H2 est dans le classpath.");
            e.printStackTrace();
        }
    }
}
