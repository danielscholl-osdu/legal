# legal-gcp

os-legal-gcp is a Spring Boot service that hosts CRUD APIs that enable management of legal tags within the OSDU R2 ecosystem.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.
 
### Prerequisites
 
- [Maven 3.6.0+](https://maven.apache.org/download.cgi)
- [AdoptOpenJDK8](https://adoptopenjdk.net/)
- [Lombok 1.16 or later](https://projectlombok.org/setup/maven)
- [GCloud SDK with java (latest version)](https://cloud.google.com/sdk/docs/install)

# Features of implementation
This is a universal solution created using EPAM OSM and OBM mappers technology.
It allows you to work with various implementations of KV stores and Blob stores.

## Limitations of the current version

In the current version, the mappers are equipped with several drivers to the stores:

- OSM (mapper for KV-data): Google Datastore; Postgres
- OBM (mapper to Blob stores): Google Cloud Storage (GCS); MinIO
- OQM (mapper to message brokers): Google PubSub; RabbitMQ

## Extensibility

To use any other store or message broker, implement a driver for it. With an extensible set of drivers, the solution is unrestrictedly universal and portable without modification to the main code.

Mappers support "multitenancy" with flexibility in how it is implemented.
They switch between datasources of different tenants due to the work of a bunch of classes that implement the following interfaces:

- Destination - takes a description of the current context, e.g., "data-partition-id = opendes"
- DestinationResolver – accepts Destination, finds the resource, connects, and returns Resolution
- DestinationResolution – contains a ready-made connection, the mapper uses it to get to data

## Mapper tuning mechanisms

This service uses specific implementations of DestinationResolvers based on the tenant information provided by the OSDU Partition service.
- for Google Datastore: osm/DsTenantDestinationResolver.java
- for Postgres: osm/PgTenantDestinationResolver.java
- for MinIO: obm/MinioDestinationResolver.java
- for RabbitMQ: oqm/MqTenantOqmDestinationResolver.java

#### Their algorithms are as follows:
- incoming Destination carries data-partition-id
- resolver accesses the Partition service and gets PartitionInfo
- from PartitionInfo resolver retrieves properties for the connection: URL, username, password etc.
- resolver creates a data source, connects to the resource, remembers the datasource
- resolver gives the datasource to the mapper in the Resolution object
- Google Cloud resolvers do not receive special properties from the Partition service for connection,
  because the location of the resources is unambiguously known - they are in the GCP project.
  And credentials are also not needed - access to data is made on behalf of the Google Identity SA
  under which the service itself is launched. Therefore, resolver takes only
  the value of the **projectId** property from PartitionInfo and uses it to connect to a resource
  in the corresponding GCP project.

# Configuration

## Service Configuration

Define the following environment variables.
Most of them are common to all hosting environments, but there are properties that are only necessary when running in Google Cloud.

In order to run the service locally, you will need to have the following environment variables defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `legal` | Logging prefix | no | - |
| `AUTHORIZE_API` | `https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `LEGAL_HOSTNAME-NAME` | ex `os-legal-dot-opendes.appspot.com` | Legal hostname| no | - |
| `GCLOUD_PROJECT` | ex `osdu-cicd-epam` | Google cloud project id | no | -- |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json` | Service account credentials, you only need this if running locally | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `RECORDS_ROOT_URL` | ex `https://os-storage-dot-nice-etching-277309.uc.r.appspot.com/api/storage/v2` / Storage API endpoint | no | output of infrastructure deployment |
| `REDIS_GROUP_HOST` | ex `127.0.0.1` | Redis host for groups | no | https://console.cloud.google.com/memorystore/redis/instances |
| `REDIS_STORAGE_HOST` | ex `127.0.0.1` | Redis host for storage | no | https://console.cloud.google.com/memorystore/redis/instances |
| `REDIS_GROUP_PORT` | ex `6379` | Redis port for groups | no | https://console.cloud.google.com/memorystore/redis/instances |
| `REDIS_STORAGE_PORT` | ex `6379` | Redis port for storage | no | https://console.cloud.google.com/memorystore/redis/instances |
| `PARTITION_API` | ex `http://localhost:8081/api/partition/v1` | Partition service endpoint | no | - |
| `ENABLE_FULL_BUCKET_NAME` | ex `true` | Full bucket name | no | - |

#### For Mappers, to activate drivers
| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `OSMDRIVER` | `postgres` OR `datastore` | Osm driver mode that defines which KV storage will be used | no | - |
| `OBMDRIVER` | `gcs` OR `minio` | Obm driver mode that defines which object storage will be used | no | - |
| `OQMDRIVER` | `pubsub` OR `rabbitmq` | Oqm driver mode that defines which message broker will be used | no | - |

#### For Google Cloud only
| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |

## Configuring mappers' Datasources
When using non-Google-Cloud-native technologies, property sets must be defined on the Partition service as part of PartitionInfo for each Tenant.

They are specific to each storage technology:

#### for OSM - Postgres:
**prefix:** `osm.postgres`
It can be overridden by:
- through the Spring Boot property `osm.postgres.partitionPropertiesPrefix`
- environment variable `OSM_POSTGRES_PARTITIONPROPERTIESPREFIX`

**Propertyset:**

| Property | Description |
| --- | --- |
| osm.postgres.datasource.url | server URL |
| osm.postgres.datasource.username | username |
| osm.postgres.datasource.password | password |

<details><summary>Example of a definition for a single tenant</summary>

```

curl -L -X PATCH 'https://dev.osdu.club/api/partition/v1/partitions/opendes' -H 'data-partition-id: opendes' -H 'Authorization: Bearer ...' -H 'Content-Type: application/json' --data-raw '{
  "properties": {
    "osm.postgres.datasource.url": {
      "sensitive": false,
      "value": "jdbc:postgresql://35.239.205.90:5432/postgres"
    },
    "osm.postgres.datasource.username": {
      "sensitive": false,
      "value": "osm_poc"
    },
    "osm.postgres.datasource.password": {
      "sensitive": true,
      "value": "osm_poc"
    }
  }
}'

```

</details>

#### for OBM - Minio:
**prefix:** `obm.minio`
It can be overridden by:

- through the Spring Boot property `obm.minio.partitionPropertiesPrefix`
- environment variable `OBM_MINIO_PARTITIONPROPERTIESPREFIX`

**Propertyset** (for two types of connection: messaging and admin operations):

| Property | Description |
| --- | --- |
| obm.minio.endpoint | - url |
| obm.minio.credentials.access.key | - username |
| obm.minio.credentials.secret.key | - password |

<details><summary>Example of a single tenant definition</summary>

```

curl -L -X PATCH 'https://dev.osdu.club/api/partition/v1/partitions/opendes' -H 'data-partition-id: opendes' -H 'Authorization: Bearer ...' -H 'Content-Type: application/json' --data-raw '{
  "properties": {
    "obm.minio.endpoint": {
      "sensitive": false,
      "value": "localhost"
    },
    "obm.minio.credentials.access.key": {
      "sensitive": false,
      "value": "minioadmin"
    },
    "obm.minio.credentials.secret.key": {
      "sensitive": false,
      "value": "secret"
    }
  }
}'

```

</details>

## For postgres only. Schema configuration

```
CREATE TABLE <partitionId>."LegalTagOsm"(
id text COLLATE pg_catalog."default" NOT NULL,
pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
data jsonb NOT NULL,
CONSTRAINT LegalTagOsm_id UNIQUE (id)
);
CREATE INDEX LegalTagOsm_dataGin ON public."LegalTagOsm" USING GIN (data);

```

Example of filling table with LegalTag

```

INSERT INTO <partitionId>."LegalTagOsm"(
id, data)
VALUES ('726612843', '{
  "id": 726612843,
  "name": "opendes-gae-integration-test-1639485896236",
  "isValid": true,
  "properties": {
    "COO": [
      "US"
    ],
    "dataType": "Transferred Data",
    "contractId": "A1234",
    "originator": "MyCompany",
    "personalData": "No Personal Data",
    "expirationDate": "Dec 31, 9999",
    "exportClassification": "EAR99",
    "securityClassification": "Public"
  },
  "description": ""
}');

```
#### for OQM - RabbitMQ:

**prefix:** `oqm.rabbitmq`
It can be overridden by:

- through the Spring Boot property `oqm.rabbitmq.partitionPropertiesPrefix`
- environment variable `OQM_RABBITMQ_PARTITIONPROPERTIESPREFIX`

**Property Set** (for two types of connection: messaging and admin operations):

| Property | Description |
| --- | --- |
| oqm.rabbitmq.amqp.host | messaging hostname or IP |
| oqm.rabbitmq.amqp.port | - port |
| oqm.rabbitmq.amqp.path | - path |
| oqm.rabbitmq.amqp.username | - username |
| oqm.rabbitmq.amqp.password | - password |
| oqm.rabbitmq.admin.schema | admin host schema |
| oqm.rabbitmq.admin.host | - host name |
| oqm.rabbitmq.admin.port | - port |
| oqm.rabbitmq.admin.path | - path |
| oqm.rabbitmq.admin.username | - username |
| oqm.rabbitmq.admin.password | - password |

<details><summary>Example of a single tenant definition</summary>

```

curl -L -X PATCH 'https://dev.osdu.club/api/partition/v1/partitions/opendes' -H 'data-partition-id: opendes' -H 'Authorization: Bearer ...' -H 'Content-Type: application/json' --data-raw '{
  "properties": {
    "oqm.rabbitmq.amqp.host": {
      "sensitive": false,
      "value": "localhost"
    },
    "oqm.rabbitmq.amqp.port": {
      "sensitive": false,
      "value": "5672"
    },
    "oqm.rabbitmq.amqp.path": {
      "sensitive": false,
      "value": ""
    },
    "oqm.rabbitmq.amqp.username": {
      "sensitive": false,
      "value": "guest"
    },
    "oqm.rabbitmq.amqp.password": {
      "sensitive": true,
      "value": "guest"
    },

     "oqm.rabbitmq.admin.schema": {
      "sensitive": false,
      "value": "http"
    },
     "oqm.rabbitmq.admin.host": {
      "sensitive": false,
      "value": "localhost"
    },
    "oqm.rabbitmq.admin.port": {
      "sensitive": false,
      "value": "9002"
    },
    "oqm.rabbitmq.admin.path": {
      "sensitive": false,
      "value": "/api"
    },
    "oqm.rabbitmq.admin.username": {
      "sensitive": false,
      "value": "guest"
    },
    "oqm.rabbitmq.admin.password": {
      "sensitive": true,
      "value": "guest"
    }
  }
}'

```

</details>

Check that maven is installed:

```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.mvn/community-maven.settings.xml`:

```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>community-maven-via-private-token</id>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
             <configuration>
              <httpHeaders>
                  <property>
                      <name>Private-Token</name>
                      <value>${env.COMMUNITY_MAVEN_TOKEN}</value>
                  </property>
              </httpHeaders>
             </configuration>
        </server>
    </servers>
</settings>
```

* Update the Google cloud SDK to the latest version:

```bash
gcloud components update
```
* Set Google Project Id:

```bash
gcloud config set project <YOUR-PROJECT-ID>
```

* Perform a basic authentication in the selected project:

```bash
gcloud auth application-default login
```

* Navigate to search service's root folder and run:

```bash
mvn jetty:run
## Testing
* Navigate to legal service's root folder and run:
 
```bash
mvn clean install   
```

* If you wish to see the coverage report then go to testing/target/site/jacoco-aggregate and open index.html

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
cd provider/legal-gcp/ && mvn spring-boot:run
```

## Testing

Navigate to legal service's root folder and run all the tests:

```bash
# build + install integration test core
$ (cd testing/legal-test-core/ && mvn clean install)
```

### Running E2E Tests 

This section describes how to run cloud OSDU E2E tests (testing/legal-test-gcp).

You will need to have the following environment variables defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `GCLOUD_PROJECT` | `nice-etching-277309` | google cloud project ID | yes | - |
| `MY_TENANT_PROJECT` | `osdu` | my tenant project name | yes | - |
| `INTEGRATION_TEST_AUDIENCE` | `********` | client application ID | yes | https://console.cloud.google.com/apis/credentials |
| `INTEGRATION_TESTER` | `********` | Service account for API calls. Note: this user must have entitlements configured already | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `HOST_URL` | `http://localhsot:8080/api/legal/v1/` | - | yes | - |
| `MY_TENANT` | `osdu` | OSDU tenant used for testing | yes | - |
| `SKIP_HTTP_TESTS` | ex `true` | jetty server returns 403 when running locally when deployed jettyserver is not used and the app returns a 302 so just run against deployed version only when checking http -> https redirects. Use 'true' for Google Cloud Run | yes | - |
| `ENABLE_FULL_BUCKET_NAME` | ex `true` | Full bucket name | no | - |

**Entitlements configuration for integration accounts**

| INTEGRATION_TESTER |
| ---  |
| users<br/>service.entitlements.user<br/>service.legal.admin<br/>service.legal.editor<br/>service.legal.user<br/>data.test1<br/>data.integration.test |

Execute following command to build code and run all the integration tests:

```bash
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/legal-test-core/ && mvn clean install)
$ (cd testing/legal-test-gcp/ && mvn clean test)
```

## Deployment

* Data-Lake Legal Google Cloud Endpoints on App Engine Flex environment 
  * Deploy
    ```sh
    mvn appengine:deploy -pl org.opengroup.osdu.legal:legal -amd
    ```

  * If you wish to deploy the search service without running tests
    ```sh
    mvn appengine:deploy -pl org.opengroup.osdu.legal:legal -amd -DskipTests
    ```

or
* Google Documentation: https://cloud.google.com/cloud-build/docs/deploying-builds/deploy-appengine

## Licence
Copyright © Google LLC
Copyright © EPAM Systems
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
