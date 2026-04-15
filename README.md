# Car Salvage Marketplace - Spring Boot Application

This project is a Spring Boot marketplace focused on salvage and damaged vehicles rather than a traditional car dealership website. It allows dealers to publish listings for damaged, salvage, passenger, commercial, and motorcycle vehicles, while buyers can browse inventory, filter by category and condition, review large image galleries, and contact sellers through multiple channels.

The application is built with Spring Boot, Thymeleaf, PostgreSQL, and Flyway. It also includes multilingual support, SEO metadata generation, recently viewed vehicle tracking, dashboard statistics, and image handling for 20 to 25 photos per vehicle.

## Core Features

- Salvage-focused inventory marketplace
- Vehicle categories: `DAMAGED`, `SALVAGE`, `PASSENGER_CAR`, `COMMERCIAL_VEHICLE`, `MOTORCYCLE`
- Multi-language support: English, Spanish, Dutch, German, French
- Large image galleries with validation and thumbnail generation
- Recently viewed vehicle tracking
- Dealer dashboard and category-based listing statistics
- SEO metadata and sitemap generation
- Contact interaction logging for phone, email, and WhatsApp

## Quick Start

### 1. Move into the project

```bash
cd /Users/zion/dev/project/salvage
```

### 2. Build the project

```bash
./gradlew clean build
```

### 3. Start PostgreSQL

```bash
docker compose up -d db
```

### 4. Run the application

```bash
./gradlew bootRun
```

## Development

### Run in development mode

```bash
docker compose up -d db
./gradlew bootRun
```

Spring Boot DevTools is enabled for local development, so server-side changes reload automatically.

### Build for production

```bash
./gradlew clean build
java -jar build/libs/car-salvage-0.0.1-SNAPSHOT.jar
```

## Testing

Run the full test suite:

```bash
./gradlew test
```

## Database and Migrations

Flyway migrations run automatically at startup.

To reset the local database:

```bash
docker compose down -v
docker compose up -d db
./gradlew bootRun
```

If you run into connection issues:

1. Make sure PostgreSQL is running:

```bash
docker compose ps
```

2. Check datasource configuration in your environment or `application.properties`:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

3. Recreate the database volume if needed:

```bash
docker compose down -v && docker compose up -d db
```

## Project Focus

This repository is no longer centered on a generic dealership experience. The current product direction is:

- Dealer-managed salvage and damaged vehicle inventory
- Rich vehicle listings with strict photo requirements
- Internationalized browsing experience
- Search and filtering by category and condition
- Buyer-to-dealer contact workflows with interaction tracking
- Future expansion paths for used parts and occasion vehicles
