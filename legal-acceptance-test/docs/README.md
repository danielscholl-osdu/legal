### Running E2E Tests

You will need to have the following environment variables defined.

| name                                 | value                                            | description                                                                                                                                                                                                                    | sensitive?                              | source |
|--------------------------------------|--------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------|--------|
| `HOST_URL`                           | ex `https://osdu.core-dev.gcp.gnrg-osdu.projects.epam.com/api/legal/v1/`         | -                                                                                                                                                                                                                              | yes                                     | -      |
| `MY_TENANT`                          | ex `osdu`                                        | OSDU tenant used for testing                                                                                                                                                                                                   | yes                                     | -      |
| `SKIP_HTTP_TESTS`                    | ex `true`                                        | jetty server returns 403 when running locally when deployed jettyserver is not used and the app returns a 302 so just run against deployed version only when checking http -> https redirects. Use 'true' for Google Cloud Run | yes                                     | -      |
| `BAREMETAL_PROJECT_ID`               | ex `osdu-cim-dev`                                | project id used to specify bucket name if `ENABLE_FULL_BUCKET_NAME`=true                                                                                                                                                       | no                                      | -      |
| `TEST_MINIO_ACCESS_KEY`              | ex `admin`                                       | Minio access key                                                                                                                                                                                                               | no                                      | -      |
| `TEST_MINIO_SECRET_KEY`              | `********`                                       | Minio secret                                                                                                                                                                                                                   | yes                                     | --     |
| `TEST_MINIO_URL`                     | ex `https://s3.core-dev.gcp.gnrg-osdu.projects.epam.com` | Minio url                                                                                                                                                                                                                      | --                                      |
| `PARTITION_API`                      | ex `https://osdu.core-dev.gcp.gnrg-osdu.projects.epam.com/api/partition/v1`     | Partition service host                                                                                                                                                                                                         | no                                      | --     |



Authentication can be provided as OIDC config:

| name                                            | value                                   | description                   | sensitive? | source |
|-------------------------------------------------|-----------------------------------------|-------------------------------|------------|--------|
| `TEST_OPENID_PROVIDER_CLIENT_ID`     | `********`                              | Client Id for `$INTEGRATION_TESTER`      | yes        | -      |
| `TEST_OPENID_PROVIDER_CLIENT_SECRET` | `********`                              | Client secret for `$INTEGRATION_TESTER` | yes        | -      |
| `TEST_OPENID_PROVIDER_URL`                      | `https://keycloak.com/auth/realms/osdu` | OpenID provider url           | yes        | -      |

Or tokens can be used directly from env variables:

| name                    | value      | description           | sensitive? | source |
|-------------------------|------------|-----------------------|------------|--------|
| `TEST_USER_TOKEN` | `********` | `$INTEGRATION_TESTER` Token | yes        | -      |


**Entitlements configuration for integration accounts**

| INTEGRATION_TESTER                                                                                                                                   |
|------------------------------------------------------------------------------------------------------------------------------------------------------|
| users<br/>service.entitlements.user<br/>service.legal.admin<br/>service.legal.editor<br/>service.legal.user<br/>data.test1<br/>data.integration.test |

Execute following command to build code and run all the integration tests:

```bash
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd legal-acceptance-test && mvn clean test)
```


## License

Copyright © Google LLC

Copyright © EPAM Systems

Copyright © ExxonMobil

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
