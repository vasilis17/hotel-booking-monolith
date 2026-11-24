# Hotel Booking System - Monolithic Architecture

A full-stack hotel booking application built with **Java Spring Boot** (backend) and **HTML/CSS/JavaScript** (frontend). This project is part of a thesis comparing **Monolithic** vs **Microservices** architectures.

---

## Technologies

| Layer | Technology |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 4, Spring Data JPA |
| **Security** | Spring Security, JWT (JSON Web Tokens), BCrypt |
| **Database** | PostgreSQL 15 |
| **Frontend** | HTML5, CSS3 (Bootstrap 5), JavaScript (Fetch API) |
| **Tools** | Maven, Git, Docker Compose |

---

## Features

- **User Management:** Registration & Login with secure JWT authentication.
- **Room Management:** View available rooms with images, prices, and amenities.
- **Booking System:** Real-time booking with availability check (prevents double booking).
- **User Dashboard:** View personal booking history.
- **Responsive Design:** Fully responsive UI for desktop and mobile.

---

## Architecture

This project is the **Monolithic** part of a larger comparison study.

- `monolith/` - The monolithic backend (Spring Boot)
- `frontend/` - The web frontend (HTML/CSS/JS)
- `microservices/` - (Planned) The microservices version

The monolithic backend serves all functionalities (Users, Rooms, Bookings) from a single process, whereas the upcoming microservices version will split them into independent services.

---

## How to Run

### Prerequisites
- Java 17 (or higher)
- Maven
- Modern web browser

### Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/vasilis17/hotel-booking-monolith.git
   cd hotel-booking-monolith
## Backend configuration (Windows PowerShell)

From `monolith/`, set `$env:DB_PASSWORD` to your existing local database password and `$env:JWT_SECRET` to a new Base64-encoded random 64-byte key. Then run `docker compose up -d` and `.\mvnw.cmd spring-boot:run`. Existing PostgreSQL volumes retain their original password. Room writes are disabled through HTTP until an admin role is implemented; use database seed data for the experiment.
