# Repository Guidelines

This repository contains a layered Spring Boot marketplace for damaged and salvage vehicles. It includes backend code, Thymeleaf templates, static assets, PostgreSQL integration, Flyway migrations, and MVC tests. Use this guide to stay consistent when extending the project.

## Project Structure & Module Organization

- `src/main/java/com/cardealer`: Spring Boot application code (`controller`, `service`, `repository`, `model`, `dto`, `config`, etc.).
- `src/test/java/com/cardealer`: MVC, security, and service-oriented tests.
- `src/main/resources/templates`: Thymeleaf pages and fragments.
- `src/main/resources/static`: Static assets grouped by type (`css`, `js`, `img`, `fonts`).
- `src/main/resources/db/migration`: Flyway SQL migrations.

## Build, Test, and Development Commands

- Start PostgreSQL locally:
  - `docker compose up -d db`
- Run the app:
  - `./gradlew bootRun`
- Run tests:
  - `./gradlew test`
- Asset edits: no frontend build step is required; edit files under `src/main/resources/static/` and refresh the browser.

## Coding Style & Naming Conventions

- HTML/CSS/JS: 2-space indentation; keep lines concise; prefer semantic HTML.
- Java: 4-space indentation; follow standard Spring naming.
- Filenames: use `kebab-case` for HTML/CSS/JS (e.g., `profile-setting.html`, `main.css`, `car-list.js`).
- Paths: prefer Spring Boot static asset paths such as `/js/main.js` or `/css/style.css` from templates.
- Formatting: run Prettier (optional) on templates/static; avoid inline scripts/styles when possible.

## Testing Guidelines

- Frontend: add lightweight smoke tests if introducing tooling (e.g., Playwright). Place under `tests/` and document commands.
- Use JUnit 5 with Spring Boot test support; mirror package structure under `src/test/java`.
- Aim for meaningful coverage on controllers, services, security rules, and user flows; keep UI tests fast and focused.

## Commit & Pull Request Guidelines

- Commits: use Conventional Commits (e.g., `feat: add dealer profile page`, `fix: correct asset path in header`).
- PRs: small, focused changes; include a clear description, linked issues, and screenshots/GIFs for UI changes.
- Check that modified templates load without console errors and asset paths resolve.

## Security & Configuration Tips

- Keep third-party libraries under `static/` up to date; remove unused files.
- Spring Security is already part of the stack; preserve role-based access and externalize secrets via environment variables.
- Prefer CSP-friendly patterns: no inline JS; use separate `.js` files.
