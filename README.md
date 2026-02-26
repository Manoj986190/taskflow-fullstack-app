# 🚀 TaskFlow – Secure Full Stack Task Management Application

TaskFlow is a secure, production-ready full-stack Task Management Web Application built using **Spring Boot (Java)** and **Angular (Standalone Architecture)** with **JWT-based authentication** and **PostgreSQL database**.

The project follows clean layered architecture, RESTful API principles, and stateless security best practices.

------------------------------------------------------------

## 📌 Tech Stack

### 🔹 Backend
- Java 17+
- Spring Boot
- Spring Security (Stateless JWT)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Lombok
- H2 (Test Environment)
- JUnit 5 + Mockito

### 🔹 Frontend
- Angular 21 (Standalone Components)
- Angular Router
- Angular HTTP Client
- AuthGuard
- HTTP Interceptors
- Global Toast Notification System
- Bootstrap UI

------------------------------------------------------------

## 🔐 Core Features

### 👤 Authentication Module
- User Registration with Validation
- Secure Password Hashing using BCrypt
- JWT Token Generation
- Stateless Authentication (No server session)
- Custom JSON 401 Unauthorized Response
- Angular Route Protection (AuthGuard)
- Automatic JWT Injection via HTTP Interceptor

### 🗂 Task Management Module
- Create Task
- View User-Specific Tasks
- Update Task
- Delete Task
- Status Tracking (TODO / IN_PROGRESS / DONE)
- Search & Status Filter (Angular)
- Auto UI Refresh after CRUD operations

### 🔒 Security Features
- JWT Authentication Filter
- Stateless Session Policy
- Protected REST APIs
- CORS Configuration
- Global Exception Handling
- DTO Validation using @Valid

------------------------------------------------------------

## 🧪 Testing Coverage

### ✅ Backend Testing
- Controller Layer – MockMvc
- Service Layer – Mockito
- Repository Layer – H2 In-Memory Database
- Validation Testing
- Security Testing (@WithMockUser)
- Integration Tests (SpringBootTest)

### ✅ Frontend Testing
- Component Unit Tests
- Service Tests
- Guard Tests
- Interceptor Tests
- Toast Service Tests

Run tests:

```bash
./mvnw clean test
ng test

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
│   ├── exception/
│   └── dto/
│
└── frontend/
    └── src/app/
        ├── pages/
        ├── services/
        ├── guards/
        ├── interceptors/
        └── components/

------------------------------------------------------------

⚙️ Backend Setup

1️⃣ Navigate to backend
cd backend

2️⃣ Configure PostgreSQL (application.yml)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskflow_db
    username: postgres
    password: your_password

3️⃣ Run backend
./mvnw spring-boot:run

Backend runs at:
http://localhost:8080

------------------------------------------------------------

💻 Frontend Setup

1️⃣ Navigate to frontend
cd frontend

2️⃣ Install dependencies
npm install

3️⃣ Run Angular
ng serve

Frontend runs at:
http://localhost:4200

------------------------------------------------------------

🔐 Authentication Flow

1.User registers → Password hashed using BCrypt
2.User logs in → JWT token generated
3.JWT stored in browser (localStorage)
4.AuthGuard protects dashboard route
5.HTTP Interceptor attaches token automatically
6.Spring Security validates JWT for protected endpoints

------------------------------------------------------------

🔄 Environment Profiles

Production
 .PostgreSQL
 .Secure JWT Secret (store in environment variable)

Test
 .H2 In-Memory Database
 .@ActiveProfiles("test")
 .Isolated test environment

------------------------------------------------------------

⚠️ Security Note
⚠️ JWT secret is configured for development purposes.

For production:

 .Store JWT secret in environment variables
 .Do NOT hardcode secrets in source code
 .Use HTTPS

------------------------------------------------------------

📊 Project Status

✔ Authentication Module Complete
✔ Secure Backend APIs
✔ Angular Dashboard with Task CRUD
✔ Unit & Integration Testing Completed
✔ Production-ready architecture

------------------------------------------------------------

👨‍💻 Author

Manoj Kumar Sahoo
Full Stack Developer (Java + Angular)

------------------------------------------------------------

📜 License

This project is developed for academic and learning purposes.


