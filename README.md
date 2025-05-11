# Paridhi Portal 2025

Backend portal application for Paridhi 2025 - A Spring Boot-based event management system.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Configuration](#configuration)
- [Running the Application](#running-the-application)
  - [Development Mode](#development-mode)
  - [Production Mode](#production-mode)
  - [Docker Deployment](#docker-deployment)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Security](#security)
- [Caching](#caching)
- [File Storage](#file-storage)
- [Email Service](#email-service)
- [Testing](#testing)
- [Contributing](#contributing)

## Overview

Paridhi Portal 2025 is a comprehensive backend application developed for managing the "Paridhi" event organized by Megatronix. The system provides a robust platform for user registration, authentication, event management, team formation, and communication with participants.

The portal serves as the central hub for both event organizers and participants, streamlining processes like user registration, event creation, team registration, and event execution through a secure and scalable architecture.

## Features

- **User Authentication and Authorization**
  - JWT-based authentication system
  - Role-based access control (USER, ADMIN, SUPERADMIN)
  - Email verification with OTP
  - Password reset functionality

- **User Management**
  - User registration and profile management
  - Member Registration Details (MRD) system with unique GID (Paridhi ID) generation
  - Profile creation and management

- **Event Management**
  - Event creation, updating, and deletion
  - Event categorization by domain and type
  - Event registration status management
  - Event pricing and prize pool management

- **Team Management**
  - Team registration for events
  - Team management with unique TID (Team ID) generation
  - Combo event registration support

- **Cordinators Registration Desk (CRD)**
  - Management of event preliminaries and finals
  - Participant qualification tracking
  - Payment status tracking

- **Content Management**
  - Gallery management for event photos
  - Domain poster management
  - Team photo management

- **Email Notifications**
  - Registration confirmations
  - Event updates
  - Team creation confirmations
  - Qualification notifications

- **Administrative Features**
  - User and team management dashboard
  - Payment tracking
  - Event status management
  - Team statistics and metrics

## Technology Stack

- **Backend Framework**: Spring Boot 3.4.4
- **Programming Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **API Documentation**: Postman Collection
- **File Storage**: Cloudinary
- **Email Service**: JavaMail with SMTP
- **Containerization**: Docker & Docker Compose
- **Caching**: Caffeine
- **Testing**: JUnit with Spring Boot Test
- **Logging**: SLF4J with Logback

## Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.6+ (or use the included Maven wrapper)
- MySQL 8.0+ (or Docker for containerized deployment)
- Git

### Installation

1. Clone the repository:

```bash
git clone https://github.com/AtulSingh11-0/Paridhi-Portal-2025.git
cd Paridhi-Portal-2025
```

1. Install dependencies using Maven:

```bash
./mvnw install
```

### Configuration

1. Create a `.env` file in the project root based on the provided `.env.example`:

```properties
SPRING_PROFILES_ACTIVE=dev
SPRING_APPLICATION_NAME=Paridhi-Portal-2025

DATABASE_URL=jdbc:mysql://mysql:3306/Paridhi-Portal-2025?createDatabaseIfNotExist=true
DATABASE_USERNAME=root
DATABASE_PASSWORD=your_password

JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=86400000

SUPER_ADMIN_EMAIL=admin@example.com
SUPER_ADMIN_PASSWORD=StrongPassword123
SUPER_ADMIN_NAME=SuperAdmin

EMAIL_USERNAME=your-email@gmail.com
EMAIL_APP_PASSWORD=your_app_password
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
CLOUDINARY_URL=your_cloudinary_url
```

1. Database setup:
   - Create a MySQL database or use Docker Compose to start MySQL
   - The application can create tables automatically in development mode

## Running the Application

### Development Mode

```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

The application will start on `http://localhost:8080`.

### Production Mode

```bash
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

For production deployment, make sure to set all required environment variables.

### Docker Deployment

1. Build the Docker image:

```bash
./mvnw clean package -DskipTests
docker build -t cocatul11/paridhi-portal-2025 .
```

1. Run with Docker Compose:

```bash
docker-compose up -d
```

This will start both the MySQL database and the application containers.

## API Documentation

The API documentation is available as a Postman Collection:

- `Paridhi-Portal-2025.postman_collection.json` - Import this file into Postman to explore the API endpoints.

Key API endpoints:

- **Authentication**: `/api/auth/**`
- **User Management**: `/api/users/**`
- **Event Management**: `/api/events/**`
- **Team Management**: `/api/teams/**`
- **Combo Management**: `/api/combos/**`
- **MRD Management**: `/api/mrd/**`
- **CRD Management**: `/api/crd/**`
- **Gallery Management**: `/api/galleries/**`

## Project Structure

The project follows a standard Spring Boot structure:

```text
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── megatronix/
│   │   │           └── paridhi/
│   │   │               ├── config/         # Application configuration
│   │   │               ├── constant/       # Constants and enumerations
│   │   │               ├── controller/     # REST API controllers
│   │   │               ├── dto/            # Data Transfer Objects
│   │   │               ├── exception/      # Custom exceptions
│   │   │               ├── filter/         # Web filters
│   │   │               ├── model/          # Entity classes
│   │   │               ├── repository/     # Data repositories
│   │   │               ├── security/       # Security configuration
│   │   │               ├── service/        # Business logic
│   │   │               └── util/           # Utility classes
│   │   └── resources/
│   │       ├── application.properties      # Common configuration
│   │       ├── application-dev.properties  # Development configuration
│   │       ├── application-prod.properties # Production configuration
│   │       └── logback-spring.xml          # Logging configuration
│   └── test/
│       └── java/                           # Test classes
├── .env                                    # Environment variables
├── docker-compose.yml                      # Docker Compose configuration
├── Dockerfile                              # Docker image definition
├── mvnw                                    # Maven wrapper script
├── pom.xml                                 # Maven configuration
└── README.md                               # This documentation
```

## Security

The application uses JWT token-based authentication implemented with Spring Security. Users can have the following roles:

- **ROLE_USER**: Regular users who can register for events and manage their own teams
- **ROLE_ADMIN**: Administrators who can manage events, teams, and users
- **ROLE_SUPERADMIN**: Super administrators with full access to all system features

Security features include:

- CORS and CSRF protection
- Password encryption with BCrypt
- Email verification
- Token-based authentication
- Role-based access control
- Method-level security with Spring Security annotations

## Caching

The application uses Caffeine for caching frequently accessed data:

- Events
- Users
- Teams
- Galleries

Cache configuration can be tuned in the application properties files.

## File Storage

Cloudinary is used for file storage, enabling the management of:

- Event images
- Gallery photos
- Domain posters
- Team photos
- User profile pictures

## Email Service

The application sends transactional emails for:

- User registration and verification
- Password reset
- MRD registration confirmation
- Team registration confirmation
- Event qualification notifications

## Testing

The application includes unit and integration tests. Run the tests with:

```bash
./mvnw test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests
5. Submit a pull request
