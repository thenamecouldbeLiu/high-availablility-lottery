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
4. Keycloak and user related service
The default Keycloak admin user for master realm is:
- Username: admin
- Password: admin
The default Keycloak admin user for lottery realm is:
- Username: lottery_admin
- Password: password

For all new established users, they are assigned a default role of "NORMAL_USER" in the lottery realm.
You can manage user roles and permissions through the Keycloak admin console or admin api provided in user service.
The project saves user and keycloak id information to db for future extension of user info and prevents the dependency
toward keycloak (but not exactly add other tables as other information of user in this project).

### Update a Keycloak user

An administrator can update Keycloak identity data and the corresponding local searchable user data with:

```http
PUT /api/keycloak/users/{keycloakUserId}
Authorization: Bearer <admin-access-token>
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@example.com",
  "firstName": "Alice",
  "lastName": "Chen",
  "displayName": "Alice Chen",
  "enabled": true,
  "roles": ["NORMAL_USER"],
  "attributes": {
    "locale": ["zh-TW"]
  }
}
```

`username`, `email`, `displayName`, and `enabled` are also updated in `app_user`. Roles remain exclusively in
Keycloak. The endpoint only manages the application realm roles `ADMIN` and `NORMAL_USER`; unrelated Keycloak roles
are preserved. The `user-service` service account requires the `realm-management` client roles `manage-users`,
`view-users`, and `view-realm`. These roles are included in the local realm export.

5. Initialize example campaigns

The [campaign request file](init-service/campaign-requests.http) creates and activates two example campaigns:

- `ANNIVERSARY_2026`: 2026 anniversary lottery with a maximum of five draws per user.
- `MID_AUTUMN_2026`: 2026 Mid-Autumn lottery with a maximum of three draws per user.

Each campaign contains exactly three prizes and one no-prize item. The enabled prize probabilities add up to `1.0`,
which is required by `CampaignService` before a campaign can be activated.

To initialize the campaigns:

1. Start PostgreSQL, Keycloak, the user service, and the lottery service.
2. Sign in as `lottery_admin` and obtain an access token containing the `ADMIN` role. The default password in the
   development realm is `password`.
3. Open `init-service/campaign-requests.http` in IntelliJ IDEA.
4. Replace `replace-with-admin-access-token` in the `adminToken` variable with the access token.
5. Run all requests from top to bottom. The file automatically captures both campaign IDs from the create responses.
6. Run the final `GET /api/lottery/campaigns` request to verify both active campaigns.

The campaign codes are unique. Running the create requests again against the same database returns a duplicate-request
error; delete the existing campaigns or change the campaign codes before rerunning them.

6. Reset and reinitialize the Docker environment

> **Warning:** The following commands permanently delete the local PostgreSQL, Redis, and RabbitMQ Docker volume data.
> This removes application data, Keycloak users/configuration, cached data, queues, campaigns, prizes, and draw history.

First stop the locally running user and lottery application processes. From the project root, run:

```bash
docker compose -f init-service/docker-compose.yml down --volumes --remove-orphans
docker compose -f init-service/docker-compose.yml up -d --force-recreate
```

The first command removes the containers and the three named volumes declared by this project:

- `postgres-data`: application data and the Keycloak database.
- `redis-data`: cache and idempotency data.
- `rabbitmq-data`: queues and messages.

The second command recreates PostgreSQL, Redis, RabbitMQ, and Keycloak. On a clean PostgreSQL volume,
`init-service/postgres/init-keycloak.sql` recreates the Keycloak database, and Keycloak imports
`init-service/keycloak-export/lottery-realm.json`.

Wait until all containers are ready:

```bash
docker compose -f init-service/docker-compose.yml ps
docker compose -f init-service/docker-compose.yml logs -f postgres keycloak
```

After PostgreSQL and Keycloak are healthy, press `Ctrl+C` to leave the log view and start the application services:

```bash
./dev.sh
```

Liquibase recreates the application schema when the user and lottery services start. To restore the two example
campaigns, execute `init-service/campaign-requests.http` from top to bottom after obtaining a new admin access token.
Tokens issued before the reset are invalid because the Keycloak realm was recreated.

7. Unit tests and JaCoCo coverage report

Run the lottery service unit tests from the project root:

```bash
./gradlew :lottery:test
```

The test task automatically generates JaCoCo reports after the tests finish:

- HTML: `lottery/build/reports/jacoco/test/html/index.html`
- XML: `lottery/build/reports/jacoco/test/jacocoTestReport.xml`

To generate only the report explicitly (the task also runs the required tests):

```bash
./gradlew :lottery:jacocoTestReport
```

The service unit tests use mocks and do not require Docker, PostgreSQL, Redis, or RabbitMQ.
