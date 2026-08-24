1. Introduction
The lottery service is formed by 3 subprojects: 
- Common: This contains shared utilities, classes (e.g. Response, Exception, etc...) and configurations used across the other sub-projects.
- Lottery service: This is the backend service that handles all the lottery operations, including campaign management, 
prize drawing. 
- User service: This is the backend service that handles user management through Keycloak, including user registration, authentication, and authorization.
For simplicity, the user can only register themselves through keycloak ui. Email verification/IDP login is not required for this project. The user can log in to the lottery service using their Keycloak credentials after registration.
These additional features can be added in Keycloak UI with SMTP/IDP configuration. The lottery service is built using Spring Boot and exposes REST APIs for the frontend to interact with. 

2. Prerequisites
- Java 26 (didn't test with other versions)
- Springboot 4
- Keycloak 26.4 （use PostgreSQL as db）
- the full config is in the ./K8S folder, you can use docker-compose to start the services.

3. Starting the services
To start the services, you can use the following commands:
   - cd ./init-service
   - docker-compose up -d
   - start lottery/user service on your IDE, or use the following commands to start the services:
     - cd ./lottery-service
     - ./mvnw spring-boot:run
     - cd ./user-service
     - ./mvnw spring-boot:run
   
   for more engineering method, you can directly call dev.sh in your VM or cloud env.
4. Default keycloak admin user
The default Keycloak admin user for master realm is:
- Username: admin
- Password: admin
The default Keycloak admin user for lottery realm is:
- Username: lottery_admin
- Password: password

for all new established users, they are assigned a default role of "NORMAL_USER" in the lottery realm. 
You can manage user roles and permissions through the Keycloak admin console.
