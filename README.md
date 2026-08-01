# 🚀 Reddit CLI & Spring Boot API

A command-line interface (CLI) Reddit prototype built on a modern Client-Server architecture. This project evolved from a monolithic application with local storage (JSON) to a distributed system, utilizing a robust Spring Boot backend and a PostgreSQL database.

## 🏗️ Architecture
*   **Backend (Server):** Spring Boot REST API. Handles business logic, validations and data persistence.
*   **Frontend (Client):** Java CLI. An interactive console interface that communicates exclusively via HTTP requests with the server. The client is stateless (no local data storage).
*   **Database:** PostgreSQL (hosted on Neon.tech).

## ✨ Core Features

### 👤 Account Management
*   Registration and Authentication (HTTP session-based system).
*   Secure password change (using the email address associated with the account).
*   Robust password and email format validation handled entirely on the server (via Spring Validation).
*   Account deletion (cascading deletion for all associated posts and comments).

### 📝 Posts and Subreddits
*   Create, view, and delete posts.
*   Voting system (Upvote / Downvote) uniquely tied to each user.
*   Filter and navigate posts based on Subreddit categories.

### 💬 Comments (Tree Structure)
*   Add comments to existing posts.
*   "Reply" system to create discussion threads (nested comments / comment trees).
*   Delete and vote on comments by delegating complex recursive search operations to the backend.

## 🛠️ Technologies Used
*   **Java 26**
*   **Spring Boot** (Spring Web, Spring Data JPA, Hibernate)
*   **PostgreSQL** (Relational Database)
*   **Java HttpClient** (For communication between the CLI and the API)

## 🚀 Installation & Setup

### 1. Database & Backend Configuration
1.  Ensure you have a [Neon.tech](https://neon.tech/) account and an active PostgreSQL database.
2.  Open the `application.properties` file in the Spring Boot project and update your credentials:
    ```properties
    spring.datasource.url=jdbc:postgresql://<neon-url>/<db-name>
    spring.datasource.username=<your-username>
    spring.datasource.password=<your-password>
    spring.jpa.hibernate.ddl-auto=update
    ```
3.  Start the Spring Boot application. The server will run on `http://localhost:8080` by default.

### 2. Running the Client (CLI)
1.  Ensure the Spring Boot server is up and running without errors.
2.  Compile and run the `main` method in the main class of the CLI project.
3.  The console application will start and automatically connect to the API. If the server is offline, the CLI will handle the error gracefully, preventing any data corruption or desynchronization.

## 🧹 About the Refactoring Journey
This project underwent a major architectural rewrite to adhere to standard software engineering best practices:
*   The legacy *dual-write* logic and local `.json` file storage were completely removed. The PostgreSQL database is now the strict source of data.
*   Manual ID generation, recursive in-memory list searches, and entity relationship mapping were delegated to native database features (Foreign Keys, Auto-Increment, SQL queries).
*   The CLI was entirely decoupled from the business logic, transforming it into a interface responsible solely for capturing user input and displaying HTTP responses.
