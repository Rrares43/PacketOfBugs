# CLI Reddit

A Command-Line Interface (CLI) application built in Java that simulates the core functionalities of Reddit.

Currently, the application runs entirely in the terminal and uses a local JSON file as a mock database to persist data between sessions. This project serves as a foundational architecture that will progressively scale into a fully-fledged backend application.

---

## 🚀 Current Features

* **Subreddit & Post Management:** Create posts and associate them with specific subreddits.
* **Interaction System:** Upvote or downvote posts to influence their ranking.
* **Comment Trees:** Add comments to posts and reply to specific comments.
* **Account Simulation:** Basic mock user session management.
* **Data Persistence:** All posts, comments, and votes are saved locally in a `App/data/reddit_database.json` file.

---

## 🛠️ Tech Stack (Current State)

* **Language:** Java
* **Data Storage:** Local JSON File
* **Libraries:** Gson (for JSON serialization/deserialization)
* **Architecture:** Custom Controller-Service-Repository pattern with SOLID principles in mind.

---

## 📦 Getting Started

Since the project currently does not use a build tool like Maven or Gradle, you will need to include the Gson `.jar` file manually to compile and run the application.

### Prerequisites

* Java Development Kit (JDK) installed.
* The `gson-2.14.0.jar` file (included in the `libs/` directory).

### How to Run (Command Line)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Rrares43/CLI_Reddit.git
   ```

2. **Navigate to the src directory:**
   ```bash
   cd CLI_Reddit/App/App/src
   ```

3. **Compile the code** *(ensure the Gson jar is in your classpath)*:
   ```bash
   javac -cp ".;../libs/gson-2.14.0.jar" Main.java
   ```

4. **Run the application:**
   ```bash
   java -cp ".;../libs/gson-2.14.0.jar" Main
   ```

> **Note:** If you are on Linux/macOS, replace the semicolon `;` in the classpath with a colon `:`.

---

## 🗺️ Roadmap (Future Plans)

This project is actively evolving. The upcoming milestones include:

* **Relational Database Integration:** Replacing the current JSON mock database with a robust SQL database (e.g., PostgreSQL or MySQL).
* **Spring Boot Migration:** Refactoring the application to use the Spring Framework for dependency injection and RESTful API endpoints.
