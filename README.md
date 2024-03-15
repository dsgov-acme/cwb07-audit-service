# dsgov-acme Audit Service

## Overview

The dsgov-acme Audit Service is responsible for receiving, storing, and retrieving auditable events for the
various business applications in a domain agnostic way. Specifically the Audit service provides:

1. A REST-ful endpoint for other services to POST auditable events (for specific business objects)
   to be stored. These records must be stored in an immutable fashion. Clients are not free to
   delete or modify records.
2. A REST-ful endpoint for other services to retrieve the auditable events related to a specific
   business object (for use in UIs and internal workflows). As it is anticipated that any given
   business object will only have a small set of audit records, no filtering is to be provided.

The Audit Service may experience significant spikes in load, especially if the dependent services
encounter spikes in demand, or we introduce bulk operations within those business applications.
Additionally, the client applications should not have to wait on the auditing process to complete,
from the client perspective it should be a fire-and-forget process. With this in mind, the API
should act simply as a facade over a buffer/queue and should respond with a 200 as soon as the
queue push is successful.

The immutability of these records is vital to the integrity of the overall system. At a minimum
the REST endpoints should not allow any deletion of records, and the process for storing new
records should anticipate ID collisions with existing records - there should not be a uniqueness
constraint based on any identifier provided by a client service. Ideally, the database user
principal should be authorized with just enough permissions to insert and retrieve records but
not to edit or remove them.

The Audit Service is a platform component. It should be very opinionated about a small set of
required data fields (the header) and unopinionated about the majority of the record (the body).
The implication of this is that the service should be able to manage.

### Further Documentation

- [architecture diagrams](./docs/architecture/README.md)
- [tools and frameworks](./docs/tools.md)

#### Prerequisites

Make sure you have the following installed:

1. Java 11+
2. Docker
3. Setup and configure minikube (using [This setup](https://github.com/dsgov-acme/cwb07-local-environment))

#### Checkstyle

1. Install the Checkstyle plugin for IntelliJ
2. Set Checkstyle version to 8.25 in IntelliJ Preferences under Tools/Checkstyle
3. Import Checkstyle config file from `config/checkstyle/checkstyle.xml`

##### Run Locally

1. To just spin up the service in `minikube`, run this command: `skaffold run`
2. [view docs](http://api.cwb07.test/as/swagger-ui/index.html)

## Develop Locally

1. In a standalone terminal, run: `skaffold dev`
2. You should eventually have console output similar to this:
![Skaffold Dev 1](docs/assets/skaffold-dev-log-1.png)
![Skaffold Dev 2](docs/assets/skaffold-dev-log-2.png)
3. As you make code changes, Skaffold will rebuild the container image and deploy it to your local `minikube` cluster.
4. Once the new deployment is live, you can re-generate your Postman collection to test your new API changes!

To exit `skaffold dev`, in the terminal where you executed the command, hit `Ctrl + C`.

**NOTE: This will terminate your existing app deployment in minikube.**

## Validate Deployment Readiness

1. Run `./gradlew clean build` to check that the app builds and passes tests.
2. If you made changes to the Helm chart, run `skaffold render -p dev` to check YAML validity.

## Querying Postgres locally via IntelliJ

1. Open the database tab in the top right
2. Add new datasource `PostgresSQL`
3. Add host as `db.cwb07.test` and the port as `30203`
4. Add your database as `local-audit-service-db`
5. Add your user as `root` and password as `root`
6. Hit apply and save

## Deploy to GCP

*Under Construction*

## Configuration Parameters

Here are the key configuration parameters for the application:
### Helm
#### Postgres
- POSTGRES_HOST: `<db-host-instance-name>`
- POSTGRES_DB: `<db-name>`
- POSTGRES_PASSWORD: `<db-password>`
- POSTGRES_PORT: `<db-port>`
- POSTGRES_USER: `<db-user>`

#### Network
- host: `<api-domain-name>`
- applicationPort: `<k8s-application-container-port>`
- servicePort: `<k8s-service-port>`
- contextPath: `<k8s-ingress-context-path>`
- readinessProbe.path: `<k8s-readiness-probe-path>`

#### Environment Variables
- ALLOWED_ORIGINS: `<allowed-origins>`
- CERBOS_URI: `<cerbos-uri>`
- DB_CONNECTION_URL: `<db-connection-url>`
- DB_USERNAME: `<db-username>`
- DB_PASSWORD: `<db-password>`
- GCP_PROJECT_ID: `<gcp-project-id>`
- SELF_SIGN_PUBLIC_KEY: `<secret-manager-path-to-rsa-public-key>`
- SELF_SIGN_PRIVATE_KEY: `<secret-manager-path-to-rsa-private-key>`
- USER_MANAGEMENT_BASE_URL: `<user-management-base-url>`
- ENABLE_PUB_SUB: `<bool>`
- PUB_SUB_TOPIC: `<topic-name>`
- PUB_SUB_TOPIC_SUBSCRIPTION: `<subscription-name>`
- TOKEN_PRIVATE_KEY_SECRET: `<token-private-key-secret-name>`
- TOKEN_PRIVATE_KEY_VERSION: `<token-private-key-secret-version>`
- TOKEN_ISSUER: `<token-issuer-name>`
- OTEL_SAMPLER_PROBABILITY: `<opentelemetry-sampler-probability`

### Gradle

#### settings.gradle
- rootProject.name = `<project-name>`

#### gradle-wrapper.properties
- distributionBase=`<distribution-base>`
- distributionPath=`<distribution-path>`
- distributionUrl=`<distribution-url>`
- zipStoreBase=`<zip-store-base>`
- zipStorePath=`<zip-store-path>`

## Contributors

The dsgov-acme Audit Service was originally a private project with contributions from:

- [@lnavarette](https://github.com/lnavarette)
- [@davidkiss-dsgov-acme](https://github.com/davidkiss-dsgov-acme)
- [@franklincm](https://github.com/franklincm)
- [@404htm](https://github.com/404htm)
- [@jimmie-potts](https://github.com/jimmie-potts)
