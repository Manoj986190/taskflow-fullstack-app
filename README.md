# 🗂 TaskFlow – Full Stack Task Management Application

TaskFlow is a full-stack Task Management Web Application built using:

Backend: Spring Boot (Java)
Frontend: Angular (Standalone Architecture)
Database: PostgreSQL
Authentication: JWT (JSON Web Token)
Security: Spring Security (Stateless)

This project follows layered architecture and secure authentication practices.

------------------------------------------------------------

🚀 Features Implemented (Phase 1 & 2)

🔐 Authentication Module
- User Registration
- User Login
- Password Hashing using BCrypt
- JWT Token Generation
- Stateless Authentication
- Custom Unauthorized Response (401 JSON)
- Route Protection using Angular AuthGuard
- Automatic JWT Attachment using HTTP Interceptor

🔒 Security Features
- Stateless Session (No server-side session storage)
- JWT validation filter
- Protected APIs
- CORS configuration for frontend-backend communication

------------------------------------------------------------

🏗 Tech Stack

Backend:
- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Lombok

Frontend:
- Angular 21 (Standalone Components)
- Angular Router
- Angular HTTP Client
- AuthGuard
- HTTP Interceptor

------------------------------------------------------------

📁 Project Structure

taskflow-fullstack-app/
│
├── backend/
│   ├── config/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── security/
│   └── exception/
│
└── frontend/
    ├── src/app/
    │   ├── pages/
    │   ├── services/
    │   ├── guards/
    │   ├── interceptors/

------------------------------------------------------------

⚙️ Backend Setup

1. Go to backend folder:
   cd backend

2. Configure PostgreSQL in application.yaml:

   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/taskflow_db
       username: postgres
       password: your_password

3. Run backend:
   mvn spring-boot:run

Backend runs at:
http://localhost:8080

------------------------------------------------------------

💻 Frontend Setup

1. Go to frontend folder:
   cd frontend

2. Install dependencies:
   npm install

3. Run Angular app:
   ng serve

Frontend runs at:
http://localhost:4200

------------------------------------------------------------

🔐 Authentication Flow

1. User registers → Password hashed and stored
2. User logs in → JWT generated
3. JWT stored in browser localStorage
4. AuthGuard protects dashboard route
5. HTTP Interceptor attaches token to every request
6. Spring Security validates JWT for protected endpoints

------------------------------------------------------------

🧪 Test Endpoint

Protected test endpoint:

GET /api/test/secure

Accessible only with valid JWT.

------------------------------------------------------------

🧠 Production Note (Important)

Currently JWT secret key is hardcoded for development.

In production:
- Secret key must be stored in:
  - application.yml
  OR
  - Environment variable

Never hardcode secrets in source code.

This will be improved in future updates.

------------------------------------------------------------

📌 Upcoming Features (Phase 3)

- Task Entity
- User–Task Relationship
- Create Task
- Fetch Tasks (User-specific)
- Update Task
- Delete Task
- Angular Task Dashboard UI

------------------------------------------------------------

👨‍💻 Author

Manoj Kumar Sahoo

------------------------------------------------------------

📜 License

This project is for learning and educational purposes.