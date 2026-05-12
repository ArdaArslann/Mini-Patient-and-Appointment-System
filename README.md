# Mini Patient and Appointment System Project

A simple patient and appointment management application built with Spring Boot + React (Vite).

## Tech Stack

- Backend: Java 17, Spring Boot, Spring Web, Spring Data JPA
- Database: MySQL 8
- Frontend: React 19, Vite
- Container: Docker Compose

## Project Structure

- Backend: src/main/java/org/example/openemr_project
- Frontend: frontend
- Docker Compose: docker-compose.yml
- Make commands: Makefile

## Requirements

- Java 17+
- Maven 3.9+ (or the included mvnw)
- Node.js 20+
- Docker Desktop
- Make (usually available by default on macOS and Linux)

## Run the Database with Docker

From the project root:

```bash
make up
```

Check status:

```bash
make ps
```

Follow MySQL logs:

```bash
make logs
```

Open MySQL shell:

```bash
make shell
```

Stop the service:

```bash
make down
```

Remove containers and volumes:

```bash
make clean
```

### Docker MySQL Configuration

- Host: localhost
- Port: 3333
- Database: openemr
- User: root
- Password: 123456

These settings match src/main/resources/application.properties.

## Run the Backend

From the project root:

```bash
./mvnw spring-boot:run
```

Alternative:

```bash
mvn spring-boot:run
```

The backend runs on port 8080 by default.

## Run the Frontend

In a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on port 5173 by default.

## API Summary

### Patients

- GET /api/patients
- POST /api/patients
- PUT /api/patients/{id}
- DELETE /api/patients/{id}

### Appointments

- GET /api/appointments
- POST /api/appointments
- PUT /api/appointments/{id}
- DELETE /api/appointments/{id}

## Quick Start

1. make up
2. ./mvnw spring-boot:run
3. cd frontend && npm install && npm run dev
4. Open the frontend in your browser: http://localhost:5173

## Development Notes

- CORS is currently enabled only for http://localhost:5173.
- Since spring.jpa.hibernate.ddl-auto=update is enabled, tables are updated automatically.
- For production, manage credentials and connection settings via environment variables.

## License

This project was created for learning and development purposes.
