# PRI System Backand Application

## List of modules

| Module                  | Description                                                  | Application Version |
|-------------------------|--------------------------------------------------------------|---------------------|
| aggregated-test-report  | creates aggregated test report (using jacoco)                | 1.0.0               |
| auth                    | responsible for authentication                               | 1.0.0               |
| data-feed               | allow to import / export data to the system using csv format | 1.0.0               |
| defense-schedule        | responsible for defense schedule process                     | 1.0.0               |
| domain                  | the database model containing db history (liquibase)         | 1.0.0               |
| notification-management | responsible for sending notifications to users               | 1.0.0               |
| permission-management   | management of user's permissions                             | 1.0.0               |
| persistence             | the layer with database operations)                          | 1.0.0               |
| pri-application         | module containing the main class                             | 1.0.0               |
| project-management      | creating and updating the project                            | 1.0.0               |
| user-management         | responsible for user management                              | 1.0.0               |

## Technology stack:

| Name                       | Used Technology                                         |
|----------------------------|---------------------------------------------------------|
| language                   | java 17                                                 |
| building tool              | maven                                                   |
| framework                  | Spring Boot                                             |
| database                   | PostgreSQL                                              |
| database change management | Liquibase                                               |
| database for tests         | H2                                                      |
| authentication             | LDAP, jwt token                                         |
| mapping                    | MapStruct                                               |
| logging                    | SLF4j                                                   |
| unit testing               | maven surefire plugin                                   |
| integration testing        | maven failsafe plugin, RestAssured, MockMvc             |
| code coverage              | jacoco                                                  |
| others                     | lombok, springdoc-openapi, FreeMarker, OpenCSV, Jackson |

## Architecture
System is build based on the modular layered architecture.
* Model Layer - all entities are defined withing the module `domain`
* Data Access Layer - all repositories are placed in the module `persistence`
* Service and Controller layers are defined withing each functional module (respectively: `service` and `controller` packages within the module)
* Shared modules (like `permission-management` and `notification-management`) contains only service layer


## Feature flags

List of feature flags:

| Feature Flag                            | Bahaviour                                                                                                                                                |
|-----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `FF_EMAIL_TO_UNIVERSITY_DOMAIN_ENABLED` | `false` - e-mails which contains the domain defined in the variable `EMAIL_UNIVERSITY_DOMAIN` are not sent<br/> `true` - emails are sent to users always |
| `FF_LDAP_AUTHENTICATION_ENABLED`        | `false` - mock authaticantion is used<br/>`true` - real ldap is used for authentication                                                                  |

## Scheduled jobs
To enable scheduled jobs, the variable `SCHEDULED_JOBS_ENABLED` has to be set to `true`

List of scheduled jobs:

| Scheduled job                       | Behaviour                                                                                                                                                                                                                  |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `EvaluationCardFreezeScheduledJob`  | job freezes the semester evaluation card in the defense day (and creates the evaluation card in phase defense)<br/> After freezing process, user with `STUDENT` role does not see the grades till the grades are published |


## How to run application locally:

### Prerequisites

* installation of: maven, java 17
* creation of Postgres database
* substitution of all environment variables (see [config.env.example](https://github.com/System-PRI/deploy/blob/main/config.env.example) file)

#### Secrets

Secrets must be defined as `java` command arguments (see **Starting the application**)
or provided to the IDE as e.g. ENV variables - depending on the run method.

#### Authentication

To use mocked ldap authentication:

* the variable `ldap.authentication.enabled` has to be set to `false`
* mocked ldap data are in the file `ldap-mock-data.ldif`

#### Coordinator user

Coordinator user has to be added manually to the database, to the `USER_DATA` table, and has to have the
COORDINATOR role assigned (table `USER_ROLES`)
The coordinator user index number corresponds to the LDAP username.

#### Liquibase

To autogenerate an xml file with changes (e.g. when new entities were added), first navigate to the domain
module (`cd domain`), and then execute:
`mvn liquibase:diff`

(Be sure, that the database connection data are set in the `liquibase.properties` file.)

This command will generate a changes in the file: `liquibase-diffChangeLog.xml`. Then the content should be reviewed and
pasted in the target file in `resources.config.liquibase`. Finally, the new file should be added to 'changeLog.xml'
file.

#### Profile

Run application with the profile `local`

### Starting the application

To run the application use an IDE build in option (e.g. in IntelliJ) or execute in command line:

```
mvn clean package
java -jar -Dspring.profiles.active=local <environment variables> pri-application/target/pri-application-1.0-SNAPSHOT.jar 
// before execution check the name of the jar file
// as <environment variables> put all variables from config.env.example file (e.g. -DPOSTGRES_URL=${POSTGRES_URL} -DPOSTGRES_DB=${POSTGRES_DB})
```
## How to run application using docker:
Check out [deploy](https://github.com/System-PRI/deploy) repository.

## How to test the application:

Minimum required code coverage for each module (except the excluded ones): **40%**

Modules excluded from code coverage:

* aggregated-test-report
* domain
* persistence
* pri-application

`model` and `exception` packages are not counted into the code coverage.

To run the unit tests, run the command:

```
mvn clean package
```

To run all tests (unit tests and integration tests) with code coverage, run the command:

```
mvn clean verify
```

The aggregated test report is created in aggregated-test-report module (path: `target/site/jacoco-aggregate/index.html`)
