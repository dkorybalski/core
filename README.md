# PRI System Backand Application

## List of modules

| Module             | Description                                                  | Application Version |
|--------------------|--------------------------------------------------------------|---------------------|
| project-management | Creating and updating the project                            | 1.0.0               |
| data-feed          | allow to import / export data to the system using csv format | 1.0.0               |
| domain             | the database model containing db history (liquibase)         | 1.0.0               |
| persistence        | the layer with database operations)                          | 1.0.0               |
| pri-application    | module containing the main class                             | 1.0.0               |
| user-management    | responsible for user management                              | 1.0.0               |
| auth               | responsible for authentication                               | 1.0.0               |

## Technology stack:

| Name               | Used Technology |
|--------------------|-----------------|
| language           | java 17         |
| building tool      | maven           |
| framework          | Spring          |
| database           | PostgreSQL      |
| database for tests | H2              |
| logging            | SLF4j           |

## How to run application locally:

### Prerequisites

#### Secrets

Secrets must be defined as `java` command arguments (see **Starting the application**) 
or provided to the IDE as e.g. ENV variables - depending on the run method.

#### Liquibase

[//]: # (todo)

#### Profile

Run application with the profile `local`

### Starting the application

To run the application use an IDE build in option (e.g. in IntelliJ) or execute in command line:

```
mvn clean package
java -jar -Dspring.profiles.active=local -DPOSTGRES_URL=${POSTGRES_URL} -DPOSTGRES_DB=${POSTGRES_DB} -DPOSTGRES_USER=${POSTGRES_USER} -DPOSTGRES_PASSWORD=${POSTGRES_PASSWORD} -DJWT_TOKEN=${JWT_TOKEN} pri-application/target/pri-application-1.0-SNAPSHOT.jar 
// before execution check the name of the jar file
```

## How to run application using Docker:

### Prerequisites

#### Secrets

See `config.env.example` file.

#### Liquibase

[//]: # (todo)

#### Profile

Two profiles are available: `docker-prod` and `docker-dev`. 
For development purpose use `docker-dev` profile.
`docker-prod` profile is dedicated for CI/CD process.

### Starting the application

To run the application and rebuild Docker images use command:
```
docker compose -f docker-compose-${env}.yml --env-file ${path} up --build
```

To restart the application without Docker images rebuild use command:
```
docker compose -f docker-compose-${env}.yml --env-file ${path} up`
```

`${env}` possible values: `dev`,`prod`. \
`${path}` path to the config.env file (e.g. `--env-file config.env`)

## Useful links:

* Swagger UI:
  http://localhost:8080/pri/swagger-ui/index.html
* H2 database: http://localhost:8080/pri/h2/
