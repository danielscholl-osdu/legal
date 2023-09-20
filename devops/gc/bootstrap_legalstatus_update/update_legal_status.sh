#!/usr/bin/env bash
#  Copyright 2023 Google LLC
#  Copyright 2023 EPAM
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License. 

# The following script is intended for regular triggering of legal status update
#
# Expected variables:
## Common:
#  - LEGAL_HOST
#  - DATA_PARTITION_ID
## Baremetal:
#  - OPENID_PROVIDER_URL
#  - OPENID_PROVIDER_CLIENT_ID
#  - OPENID_PROVIDER_CLIENT_SECRET

set -ex

update_legal_status_baremetal() {

  DATA_PARTITION_ID=$1

  ID_TOKEN="$(curl --location --silent --globoff --request POST "${OPENID_PROVIDER_URL}/protocol/openid-connect/token" \
    --header "data-partition-id: ${DATA_PARTITION_ID}" \
    --header "Content-Type: application/x-www-form-urlencoded" \
    --data-urlencode "grant_type=client_credentials" \
    --data-urlencode "scope=openid" \
    --data-urlencode "client_id=${OPENID_PROVIDER_CLIENT_ID}" \
    --data-urlencode "client_secret=${OPENID_PROVIDER_CLIENT_SECRET}" | jq -r ".id_token")"
  export ID_TOKEN

  status_code=$(curl --location --globoff --request GET "${LEGAL_HOST}/api/legal/v1/jobs/updateLegalTagStatus" \
    --write-out "%{http_code}" --silent --output "output.txt" \
    --header "data-partition-id: ${DATA_PARTITION_ID}" \
    --header "Authorization: Bearer ${ID_TOKEN}")

  if [ "$status_code" == 204 ]; then
    echo "Legal status update completed successfully!"
  else
    echo "Legal status update failed!"
    cat /opt/output.txt | jq
    exit 1
  fi

}

update_legal_status_gc() {

  DATA_PARTITION_ID=$1

  ACCESS_TOKEN="$(gcloud auth print-access-token)"
  export ACCESS_TOKEN

  status_code=$(curl --location --globoff --request GET "${LEGAL_HOST}/api/legal/v1/jobs/updateLegalTagStatus" \
    --write-out "%{http_code}" --silent --output "output.txt" \
    --header "data-partition-id: ${DATA_PARTITION_ID}" \
    --header "Authorization: Bearer ${ACCESS_TOKEN}")

  if [ "$status_code" == 204 ]; then
    echo "Legal status update completed successfully!"
  else
    echo "Legal status update failed!"
    cat /opt/output.txt | jq
    exit 1
  fi

}

# Check variables
source ./validate-env.sh "DATA_PARTITION_ID"
source ./validate-env.sh "LEGAL_HOST"
if [[ "${ONPREM_ENABLED}" == "true" ]]; then
  source ./validate-env.sh "OPENID_PROVIDER_URL"
  source ./validate-env.sh "OPENID_PROVIDER_CLIENT_ID"
  source ./validate-env.sh "OPENID_PROVIDER_CLIENT_SECRET"
fi

# Update legal status for all partitions

if [[ "${ONPREM_ENABLED}" == "true" ]]; then
  update_legal_status_baremetal "${DATA_PARTITION_ID}"
else
  update_legal_status_gc "${DATA_PARTITION_ID}"
fi

curl -X POST http://localhost:15000/quitquitquit
exit 0
