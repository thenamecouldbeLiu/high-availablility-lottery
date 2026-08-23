The lottery service is formed by 3 subprojects: 
1. Common: This contains shared utilities, classes (e.g. Response, Exception, etc...) and configurations used across the other sub-projects.
2. Lottery service: This is the backend service that handles all the lottery operations, including campaign management, 
prize drawing. 
3. User service: This is the backend service that handles user management through Keycloak, including user registration, authentication, and authorization.
For simplicity, the user can only register themselves through keycloak ui. Email verification/IDP login is not required for this project. The user can log in to the lottery service using their Keycloak credentials after registration.
These additional features can be added in Keycloak UI with SMTP/IDP configuration. The lottery service is built using Spring Boot and exposes REST APIs for the frontend to interact with. 