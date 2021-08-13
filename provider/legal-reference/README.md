# legal-reference

os-legal-reference is a Spring Boot service that hosts CRUD APIs that enable management of legal tags within the OSDU Hybrid ecosystem.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.
 
### Prerequisites
 
- [Maven 3.6.0+](https://maven.apache.org/download.cgi)
- [AdoptOpenJDK8](https://adoptopenjdk.net/)
- [Lombok 1.16 or later](https://projectlombok.org/setup/maven)
- [GCloud SDK with java (latest version)](https://cloud.google.com/sdk/docs/install)
 
### Installation

- Setup Apache Maven
- Setup AdoptOpenJDK
- Setup GCloud SDK
- Install Eclipse (or other IDE) to run applications
- Set up environment variables for Apache Maven and AdoptOpenJDK. For example M2_HOME, JAVA_HOME, etc.
- Add a configuration for build project in Eclipse(or other IDE)

### Run Locally

### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `legal` | Logging prefix | no | - |
| `AUTHORIZE_API` | `https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `LEGAL_HOSTNAME-NAME` | ex `os-legal-dot-opendes.appspot.com` | Legal hostname| no | - |
| `MONGO_DB_URL` | ex `mongodb://localhost:27017` | Mongo DB Url| yes | output of infrastructure deployment |
| `MONGO_DB_USER` | ex `mongouser` | Mongo DB userName| yes | output of infrastructure deployment |
| `MONGO_DB_PASSWORD` | ex `mongopassword` | Mongo DB userPassword| yes | output of infrastructure deployment |
| `MONGO_DB_NAME` | ex `mongoDBName` | Mongo DB DbName| yes | output of infrastructure deployment |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |
| `MB_RABBITMQ_URI` | ex `amqp://guest:guest@127.0.0.1:5672` | MessageBroker RabbitMQ URI | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `RECORDS_ROOT_URL` | ex `https://os-storage-dot-nice-etching-277309.uc.r.appspot.com/api/storage/v2` / Storage API endpoint | no | output of infrastructure deployment |
| `PARTITION_API` | ex `http://localhost:8081/api/partition/v1` | Partition service endpoint | no | - |
| `ENABLE_FULL_BUCKET_NAME` | ex `true` | Full bucket name | no | - |

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
cd provider/legal-reference/ && mvn spring-boot:run
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
GKE Google Documentation: https://cloud.google.com/build/docs/deploying-builds/deploy-gke
Anthos Google Documentation: https://cloud.google.com/anthos/multicluster-management/gateway/tutorials/cloud-build-integration


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