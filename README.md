# Chat Engine Server Spring Application

This is a Spring Boot application for managing users and interactions with the Chat Engine API.

## Components

### 1. Configuration

- **CorsConfig**: Configuration class to handle Cross-Origin Resource Sharing (CORS) configuration.

### 2. Controllers

- **UserController**: Controller class for handling user-related HTTP requests:
    - `/login`: POST request to log in a user.
    - `/signup`: POST request to sign up a new user.
    - `/users`: GET request to retrieve all users.
    - `/users/{userId}`: DELETE request to delete a user by ID.
    - `/chats/{chatId}/removeUser`: PUT request to remove a user from a chat.

### 3. Database

- **UserRepository**: JPA repository interface for managing User entities in the database.

### 4. Model

- **User**: Entity class representing a user in the system.

### 5. Services

- **UserService**: Service class for managing user-related operations:
    - User login and signup.
    - Email verification.
    - Interaction with the Chat Engine API to manage users and chats.
    - Sending verification emails.

## Configuration

- **Application Properties**: Configuration properties for Chat Engine project ID, API key, and email settings.

## Usage

1. Clone the repository.
2. Configure the application properties (`application.properties`) with your Chat Engine project ID, API key, and email settings.
3. Run the application.
4. Access the API endpoints to interact with users and chats.

## Dependencies

- Spring Boot
- Spring Data JPA
- Gson
- Jakarta Mail API

## Contributors

- Charlie Tang
