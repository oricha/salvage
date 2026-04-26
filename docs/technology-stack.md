# Technology Stack

This document summarizes the current, real stack of the Salvage marketplace repository.

## Runtime

- Java 21
- Spring Boot 3.2.0
- Gradle (Groovy DSL)

## Backend

- Spring Web MVC
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Cache with Caffeine
- Thymeleaf plus `thymeleaf-extras-springsecurity6`

## Data

- PostgreSQL 16 for local development via Docker Compose
- Flyway for schema migrations
- Hibernate as the JPA provider

## Frontend

- Server-rendered Thymeleaf templates
- Static assets under `src/main/resources/static`
- Bootstrap 5
- jQuery
- Font Awesome
- Owl Carousel
- Isotope
- Miscellaneous helper libraries already vendored in `static/js` and `static/css`

There is no Node or npm build pipeline for the main application.

## Architecture

The application follows a layered MVC structure:

`Controller -> Service -> Repository -> PostgreSQL`

Supporting packages include DTOs, specifications for dynamic search, config classes, exception handling, validation helpers, and upload utilities.

## Development Workflow

- Start PostgreSQL with `docker compose up -d db`
- Run the app with `./gradlew bootRun`
- Run tests with `./gradlew test`
- Flyway migrations run on startup according to the active Spring profile

## Source of Truth

The stack above is inferred from:

- `build.gradle`
- `docker-compose.yml`
- `src/main/java/com/cardealer/config`
- `openspec/project.md`

If these sources and another document conflict, prefer the code and configuration files first, then `openspec/project.md`.
