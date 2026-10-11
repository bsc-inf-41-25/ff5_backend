# Ticket Master — Event Ticketing Backend

Group FF5 mini-project for Software Engineering.

## About

Spring Boot backend for an event ticketing system. Handles event discovery, ticket purchasing, customer profiles, and digital tickets with QR codes.

## Tech Stack

- Spring Boot 3.3.5
- MariaDB 11.x (local install on port 3306)
- JWT authentication (jjwt 0.12.6)
- Swagger UI for API documentation (springdoc 2.6.0)
- Maven build (via `mvnw`)

## Getting Started

### Prerequisites

- Java 17
- MariaDB 11.x running locally on port 3306
- No Maven install needed — the wrapper (`mvnw`) is included

### Setup

```bash
# Clone the repository
git clone https://github.com/bsc-inf-41-25/ff5_backend.git
cd ff5_backend

# Create the database
mysql -uroot -proot -e "CREATE DATABASE ticketmaster;"

# Run the application
./mvnw spring-boot:run