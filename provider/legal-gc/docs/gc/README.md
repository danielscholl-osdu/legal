# Service Configuration for Google Cloud


## Run args

In order to run Legal with Java 17 additional run args must be provided:

```bash
--add-opens java.base/java.lang=ALL-UNNAMED --add-opens  java.base/java.lang.reflect=ALL-UNNAMED
```

Full command:

```bash
java -jar legal.jar --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED
```

## Environment variables

Define the following environment variables.

Must have:

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `SPRING_PROFILES_ACTIVE` | ex `gcp` | Spring profile that activate default configuration for Google Cloud environment | false | - |

Defined in default application property file but possible to override:

| name                                     | value                                    | description                                                                                                                                                         | sensitive? | source                                                       |
|------------------------------------------|------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|--------------------------------------------------------------|
| `LOG_PREFIX`                             | `legal`                                  | Logging prefix                                                                                                                                                      | no         | -                                                            |
| `AUTHORIZE_API`                          | `http://entitlements/entitlements/v1`    | Entitlements API endpoint                                                                                                                                           | no         | output of infrastructure deployment                          | |
| `PARTITION_API`                          | ex `http://partition/api/partition/v1`   | Partition service endpoint                                                                                                                                          | no         | -                                                            |
| `ENABLE_FULL_BUCKET_NAME`                | ex `true`                                | Full bucket name, if `true` then bucket name will be `<project id>-<tenant name>-legal-service-configuration` otherwise `<tenant name>-legal-service-configuration` | no         | -                                                            |
| `SERVICE_TOKEN_PROVIDER`                 | `GCP`                                    | Service account token provider, `GCP` means use Google service account                                                                                              | no         | -                                                            |
| `PARTITION_AUTH_ENABLED`                 | `true`                                   | Enable auth token provisioning for requests to Partition service                                                                                                    | no         | -                                                            |
| `GOOGLE_APPLICATION_CREDENTIALS`         | ex `/path/to/directory/service-key.json` | Service account credentials, serves to gain access to cloud resources and to request tokens                                                                         | yes        | <https://console.cloud.google.com/iam-admin/serviceaccounts> |
| `PARTITION_PROPERTIES_LEGAL_BUCKET_NAME` | ex `legal.bucket.name`                   | Name of partition property for legal bucket name value                                                                                                      | yes        | -                                                            |

These variables define service behavior, and are used to switch between `baremetal` or `gc` environments, their overriding and usage in mixed mode was not tested.
Usage of spring profiles is preferred.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `PARTITION_AUTH_ENABLED` | ex `true` or `false` | Disable or enable auth token provisioning for requests to Partition service | no | - |
| `OQMDRIVER` | `rabbitmq` or `pubsub` | Oqm driver mode that defines which message broker will be used | no | - |
| `OSMDRIVER` | `datastore`| Osm driver mode that defines which KV storage will be used | no | - |
| `OBMDRIVER` | `gcs` | Obm driver mode that defines which object storage will be used | no | - |
| `SERVICE_TOKEN_PROVIDER` | `GCP` or `OPENID` |Service account token provider, `GCP` means use Google service account `OPEIND` means use OpenId provider like `Keycloak` | no | - |

### Running Locally

Mandatory variables to run service, others are located into local spring profile default env configuration.

| name                             | value      | description                                                                     | sensitive? | source                              |
|----------------------------------|------------|---------------------------------------------------------------------------------|------------|-------------------------------------|
| `SPRING_PROFILES_ACTIVE`         | `local`    | Spring profile that activate default configuration for Google Cloud environment | false      | -                                   |
| `GOOGLE_APPLICATION_CREDENTIALS` | `********` | Google Cloud service account                                                    | yes        | -                                   |

## Datastore configuration

There must be a namespace for each tenant, which is the same as the tenant name.

Example:

![Screenshot](./pics/namespace.PNG)

Kind `LegalTagOsm` will be created by service if it does not exist.

## Pubsub configuration

At Pubsub should be created topic with name:

**name:** `legaltags-changed`

It can be overridden by:

- through the Spring Boot property `pub-sub-legal-tags-topic`
- environment variable `PUB_SUB_LEGAL_TAGS_TOPIC`

Legal service responsible for publishing only. 
Consumer side `legaltags-changed` topic configuration located in
[Storage Google Cloud PubSub documentation](https://community.opengroup.org/osdu/platform/system/storage/-/blob/master/provider/storage-gc/docs/gc/README.md#pubsub-configuration)

## GCS configuration <a name="ObjectStoreConfig"></a>

### Per-tenant buckets configuration

These buckets must be defined in tenants’ “data” Google Cloud projects that names are pointed in tenants’ PartitionInfo registration objects’ “projectId” property at the Partition service.

<table>
  <tr>
   <td>Bucket Naming template
   </td>
   <td>Permissions required
   </td>
  </tr>
  <tr>
   <td>&lt;PartitionInfo.name>-legal-service-configuration

  **OR** (if $ENABLE_FULL_BUCKET_NAME == true)
<p>
&lt;PartitionInfo.projectId>-&lt;PartitionInfo.name>-legal-service-configuration
   </td>
   <td>CreateBucket, CRUDObject
   </td>
  </tr>
</table>

## Google cloud service account configuration

TBD

| Required roles |
| ---    |
| - |

### Running E2E Tests

This section describes how to run cloud OSDU E2E tests (testing/legal-test-gc).

You will need to have the following environment variables defined.

| name                      | value                                        | description                                                                                                                                                                                                                    | sensitive? | source                                                       |
|---------------------------|----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|--------------------------------------------------------------|
| `GCLOUD_PROJECT`          | `nice-etching-277309`                        | google cloud project ID                                                                                                                                                                                                        | yes        | -                                                            |
| `MY_TENANT_PROJECT`       | `osdu`                                       | my tenant project name                                                                                                                                                                                                         | yes        | -                                                            |
| `INTEGRATION_TESTER`      | `********`                                   | Service account for API calls. Note: this user must have entitlements configured already                                                                                                                                       | yes        | <https://console.cloud.google.com/iam-admin/serviceaccounts> |
| `HOST_URL`                | `http://localhsot:8080/api/legal/v1/`        | -                                                                                                                                                                                                                              | yes        | -                                                            |
| `MY_TENANT`               | `osdu`                                       | OSDU tenant used for testing                                                                                                                                                                                                   | yes        | -                                                            |
| `SKIP_HTTP_TESTS`         | ex `true`                                    | jetty server returns 403 when running locally when deployed jettyserver is not used and the app returns a 302 so just run against deployed version only when checking http -> https redirects. Use 'true' for Google Cloud Run | yes        | -                                                            |
| `ENABLE_FULL_BUCKET_NAME` | ex `true`                                    | Full bucket name                                                                                                                                                                                                               | no         | -                                                            |
| `PARTITION_API`           | ex `http://localhost:8080/api/partition/v1 ` | Partition service host                                                                                                                                                                                                         | no         | --                                                           |

**Entitlements configuration for integration accounts**

| INTEGRATION_TESTER                                                                                                                                   |
|------------------------------------------------------------------------------------------------------------------------------------------------------|
| users<br/>service.entitlements.user<br/>service.legal.admin<br/>service.legal.editor<br/>service.legal.user<br/>data.test1<br/>data.integration.test |

Execute following command to build code and run all the integration tests:

```bash
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/legal-test-core/ && mvn clean install)
$ (cd testing/legal-test-gc/ && mvn clean test)
```
