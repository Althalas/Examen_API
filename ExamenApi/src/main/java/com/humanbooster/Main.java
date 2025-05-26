package com.humanbooster;

import com.humanbooster.controller.TaskController;
import com.humanbooster.util.H2DBUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main {

    public static void main(String[] args) {
        // 1. Initialiser la base de données H2 et créer le schéma
        try {
            H2DBUtil.initializeDatabase();
            System.out.println("Base de données H2 initialisée.");
        } catch (Exception e) {
            System.err.println("Échec de l'initialisation de la base de données H2. L'application va s'arrêter.");
            e.printStackTrace();
            return; // Arrêter l'application si la BDD ne peut pas être initialisée
        }

        // 2. Configurer et démarrer le serveur Jetty
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        ServletHolder servletHolder = new ServletHolder(new TaskController());
        context.addServlet(servletHolder, "/api/*");

        server.setHandler(context);

        try {
            server.start();
            System.out.println("Serveur Jetty démarré sur le port 8080. API disponible sur http://localhost:8080/api/tasks");
            System.out.println("Appuyez sur Ctrl+C pour arrêter le serveur.");
            server.join();
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage ou de l'exécution du serveur Jetty:");
            e.printStackTrace();
            if (server.isStarted()) {
                try {
                    server.stop();
                } catch (Exception stopException) {
                    System.err.println("Erreur lors de l'arrêt du serveur:");
                    stopException.printStackTrace();
                }
            }
            System.exit(1);
        }
    }
}
