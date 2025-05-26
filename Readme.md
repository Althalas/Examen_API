# **TP API REST JAVA**

## **1\. Description du Projet**

Ce projet implémente une API RESTful pour la gestion de tâches, conformément au cahier des charges. L'API permet d'effectuer des opérations CRUD (Create, Read, Update, Delete) complètes sur des entités "Tâche" elle utilise un serveur Jetty embarqué.

Une tâche est définie par les champs suivants :

* id: Identifiant unique (Long, auto-généré)  
* title: Titre de la tâche (String, obligatoire, non vide)  
* description: Description détaillée de la tâche (String, optionnel)  
* done: Statut de la tâche (boolean, false par défaut)  
* createdAt: Date et heure de création (LocalDateTime, généré automatiquement)  
* updatedAt: Date et heure de la dernière mise à jour (LocalDateTime, mis à jour automatiquement)

## **2\. Choix Technologiques**

Les technologies suivantes ont été utilisées pour développer cette API :

* **Java 11**: Langage de programmation principal. Java 11 est une version LTS (Long-Term Support) offrant une bonne stabilité et des fonctionnalités modernes comme l'API java.time utilisée pour LocalDateTime.  
* **Maven**: Outil de gestion de projet et de build. Maven simplifie la gestion des dépendances, la compilation, le packaging et l'exécution du cycle de vie du projet.  
* **Jetty (Embarqué)**: Serveur HTTP et conteneur de servlets légers. Le choix d'un Jetty embarqué permet de packager l'application comme un simple fichier JAR exécutable, sans nécessiter de serveur d'application externe. Cela simplifie le déploiement et l'exécution.  
  * Version utilisée : 11.0.15 (compatible Jakarta EE 9+)  
* **Jakarta Servlet API**: API standard pour la création d'applications web en Java. Nous utilisons directement l'API Servlet (jakarta.servlet.\*) pour gérer les requêtes HTTP, conformément à la contrainte de ne pas utiliser de frameworks JAX-RS ou Spring MVC.  
  * Version utilisée : 5.0.0  
* **Jackson**: Bibliothèque de traitement JSON. Jackson est utilisée pour la sérialisation des objets Java (notamment les Task) en JSON pour les réponses HTTP, et la désérialisation du JSON des requêtes HTTP en objets Java. Le module jackson-datatype-jsr310 est inclus pour la gestion native des types java.time (comme LocalDateTime).  
  * Version utilisée : 2.15.2  
* **H2 Database Engine (en mémoire)**: Système de gestion de base de données relationnelle écrit en Java. H2 a été choisi pour sa légèreté et sa capacité à fonctionner en mode "en mémoire". Cela simplifie la configuration (pas de serveur de base de données externe à installer pour le développement/test) et assure que les données sont propres à chaque lancement de l'application. La table des tâches est créée au démarrage si elle n'existe pas.  
  * Version utilisée : 2.2.224  
* **JDBC (Java Database Connectivity)**: API Java standard pour l'interaction avec les bases de données. Le TaskDAO utilise JDBC pour exécuter des requêtes SQL sur la base de données H2.

## **3\. Structure du Projet (Maven Standard)**

ExamenApi/  
├── pom.xml                     \# Fichier de configuration Maven  
├── README.md                  
└── src/  
    └── main/  
        └── java/  
            └── com/  
                └── humanbooster/  
                    ├── Main.java             \# Classe principale (démarrage Jetty, init BDD)  
                    ├── model/  
                    │   └── Task.java         \# Entité métier "Tâche"  
                    ├── DAO/  
                    │   └── TaskDAO.java      \# Data Access Object (logique de persistance avec H2)  
                    ├── controller/  
                    │   └── TaskController.java  \# Controller gérant les requêtes HTTP pour les tâches  
                    └── util/  
                        └── H2DBUtil.java \# Utilitaire pour la connexion et l'initialisation de H2

## **4\. Instructions pour Lancer et Utiliser le Projet**

### **4.1. Prérequis**

* **JDK 11** (ou une version plus récente compatible) installé et configuré (variable d'environnement JAVA\_HOME).  
* **Apache Maven** (version 3.6.0 ou plus récente recommandée) installé et configuré (accessible depuis la ligne de commande).

### **4.2. Compilation du Projet**

1. Ouvrez un terminal ou une invite de commande.  
2. Naviguez jusqu'au répertoire racine du projet (où se trouve le fichier pom.xml).  
3. Exécutez la commande Maven suivante pour nettoyer le projet, compiler le code et créer un fichier JAR exécutable :  
   mvn clean package

   Cette commande va télécharger les dépendances nécessaires (si ce n'est pas déjà fait), compiler les sources, et générer un fichier JAR ExamenApi-1.0-SNAPSHOT.jar dans le répertoire target/.

### **4.3. Exécution de l'Application**

1. Après une compilation réussie, toujours dans le terminal, exécutez le fichier JAR généré avec la commande suivante :  
   java \-jar target/ExamenApi-1.0-SNAPSHOT.jar

2. Si tout se passe bien, vous devriez voir des messages dans la console indiquant :  
   * L'initialisation de la table 'tasks' dans la base de données H2.  
   * Le démarrage du serveur Jetty, généralement sur le port 8080\.  
   * Un message indiquant que l'API est disponible, par exemple : API disponible sur http://localhost:8080/api/tasks.

L'application est maintenant en cours d'exécution et prête à recevoir des requêtes HTTP. Pour arrêter le serveur, appuyez sur Ctrl+C dans le terminal.

### **4.4. Utilisation de l'API**

L'API expose les endpoints suivants, basés sur le chemin /api configuré dans Main.java :

* **GET /api/tasks**: Lister toutes les tâches.  
  * Réponse : 200 OK avec un tableau JSON des tâches.  
* **POST /api/tasks**: Créer une nouvelle tâche.  
  * Corps de la requête : Objet JSON représentant la tâche (sans id, createdAt, updatedAt). Le champ title est obligatoire.  
    {  
      "title": "Ma Nouvelle Tâche",  
      "description": "Description de la tâche",  
      "done": false  
    }

  * Réponse : 201 Created avec l'objet JSON de la tâche créée (incluant les champs auto-générés).  
  * Réponse en cas d'erreur (ex: titre manquant) : 400 Bad Request.  
* **GET /api/tasks/{id}**: Récupérer une tâche spécifique par son ID.  
  * Réponse : 200 OK avec l'objet JSON de la tâche si trouvée.  
  * Réponse si non trouvée : 404 Not Found.  
* **PUT /api/tasks/{id}**: Mettre à jour une tâche existante.  
  * Corps de la requête : Objet JSON complet de la tâche avec les modifications. Le champ title reste obligatoire.  
    {  
      "title": "Titre Mis à Jour",  
      "description": "Nouvelle description",  
      "done": true  
    }

  * Réponse : 200 OK avec l'objet JSON de la tâche mise à jour si trouvée.  
  * Réponse si non trouvée : 404 Not Found.  
  * Réponse en cas d'erreur (ex: titre manquant) : 400 Bad Request.  
* **DELETE /api/tasks/{id}**: Supprimer une tâche spécifique.  
  * Réponse : 204 No Content si la suppression réussit.  
  * Réponse si non trouvée : 404 Not Found.

**Outils pour tester l'API :**

* **curl**: Un outil en ligne de commande polyvalent pour effectuer des requêtes HTTP.  
  * Exemple (Lister les tâches) : curl \-X GET http://localhost:8080/api/tasks  
  * Exemple (Créer une tâche) :  
    curl \-X POST \-H "Content-Type: application/json" \-d '{"title":"Test avec curl","description":"Description test"}' http://localhost:8080/api/tasks

* **Postman / Insomnia**: Des clients API graphiques populaires qui facilitent la création et l'envoi de requêtes HTTP, ainsi que la visualisation des réponses.  
* **Client HTTP d'IntelliJ IDEA**: Si vous utilisez IntelliJ IDEA, vous pouvez créer un fichier .http (fourni dans Java/Requetes Task.http) pour exécuter les requêtes directement depuis l'IDE.

Exemple de fichier .http pour IntelliJ IDEA (extrait) :  
(Référez-vous au fichier complet intellij\_http\_requests\_tp\_java fourni dans les échanges précédents pour tous les cas de test).  
\#\#\# Variables  
@baseUrl \= http://localhost:8080/api  
@contentType \= application/json

\#\#\# Lister toutes les tâches  
GET {{baseUrl}}/tasks  
Accept: {{contentType}}

\#\#\# Créer une nouvelle tâche  
POST {{baseUrl}}/tasks  
Content-Type: {{contentType}}  
Accept: {{contentType}}

{  
  "title": "Tâche via client HTTP",  
  "description": "Test depuis IntelliJ"  
}

N'oubliez pas de régler l'en-tête Content-Type: application/json pour les requêtes POST et PUT qui envoient un corps JSON.