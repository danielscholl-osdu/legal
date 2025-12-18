### Running E2E Tests

You will need to have the following environment variables defined.

| name                  | value                                      | description                      | sensitive? | source | required |
|-----------------------|--------------------------------------------|----------------------------------|------------|--------|----------|
| `HOST_URL`            | ex `http://localhost:8080/api/legal/v1/`   | Legal service URL                | no         | -      | yes      |
| `MY_TENANT`           | ex `opendes`                               | OSDU tenant used for testing     | no         | -      | yes      |
| `SKIP_HTTP_TESTS`     | ex `true`                                  | Skip HTTP redirect tests (jetty server returns 403 locally; use 'true' for Google Cloud Run) | no         | -      | no       |

Authentication can be provided as OIDC config:

| name                                            | value                                      | description                                 | sensitive? | source |
|-------------------------------------------------|--------------------------------------------|---------------------------------------------|------------|--------|
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_ID`     | `********`                                 | Privileged User Client Id                   | yes        | -      |
| `PRIVILEGED_USER_OPENID_PROVIDER_CLIENT_SECRET` | `********`                                 | Privileged User Client secret               | yes        | -      |
| `TEST_OPENID_PROVIDER_URL`                      | ex `https://keycloak.com/auth/realms/osdu` | OpenID provider url                         | yes        | -      |
| `PRIVILEGED_USER_OPENID_PROVIDER_SCOPE`         | ex `api://my-app/.default`                 | OAuth2 scope (optional, defaults to openid) | no         | -      |

Or tokens can be used directly from env variables:

| name                    | value      | description           | sensitive? | source |
|-------------------------|------------|-----------------------|------------|--------|
| `PRIVILEGED_USER_TOKEN` | `********` | Privileged User Token | yes        | -      |

Authentication configuration is optional and could be omitted if not needed.

**Entitlements configuration for integration accounts**

| PRIVILEGED_USER            |
|----------------------------|
| users                      |
| service.entitlements.user  |
| service.legal.admin        |
| service.legal.editor       |
| service.legal.user         |
| data.test1                 |
| data.integration.test      |

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
