#!/usr/bin/env bash
#
# Script that bootstraps legal service
# It creates default legal tag via request to Legal service
# For now created legal tag name is hardcoded: ${DATA_PARTITION_ID}-initital-data-tag
# Contains logic for both onprem and gc version

set -ex

source ./validate-env.sh "DATA_PARTITION_ID"
source ./validate-env.sh "LEGAL_HOST"
source ./validate-env.sh "ENTITLEMENTS_HOST"
source ./validate-env.sh "DEFAULT_LEGAL_TAG"

get_token_onprem() {
    ID_TOKEN="$(curl --location --request POST "${OPENID_PROVIDER_URL}/protocol/openid-connect/token" \
        --header "Content-Type: application/x-www-form-urlencoded" \
        --data-urlencode "grant_type=client_credentials" \
        --data-urlencode "scope=openid" \
        --data-urlencode "client_id=${OPENID_PROVIDER_CLIENT_ID}" \
        --data-urlencode "client_secret=${OPENID_PROVIDER_CLIENT_SECRET}" | jq -r ".id_token")"
    export ID_TOKEN
}

get_token_gc() {
    BEARER_TOKEN=$(gcloud auth print-identity-token)
    export BEARER_TOKEN
}

check_entitlements_readiness() {
    status_code=$(curl --retry 1 --location -globoff --request GET \
        "${ENTITLEMENTS_HOST}/api/entitlements/v2/groups" \
        --write-out "%{http_code}" --silent --output "/dev/null" \
        --header 'Content-Type: application/json' \
        --header "data-partition-id: ${DATA_PARTITION_ID}" \
        --header "Authorization: Bearer ${ID_TOKEN}")

    if [ "$status_code" == 200 ]; then
        echo "$status_code: Entitlements provisioning completed successfully!"
    else
        echo "$status_code: Entitlements provisioning is in progress or failed!"
        exit 1
    fi
}

create_legaltag() {

    echo "Trying to create legal tag for initial data bootstrap"

    cat <<EOF >/opt/default_legal_tag.json
{
    "name": "${DEFAULT_LEGAL_TAG}",
    "description": "A legal tag used for uploading initial sample data",
    "properties": {
        "countryOfOrigin":["US"],
        "contractId":"No Contract Related",
        "expirationDate":"2099-01-01",
        "dataType":"Public Domain Data",
        "originator":"OSDU",
        "securityClassification":"Public",
        "exportClassification":"EAR99",
        "personalData":"No Personal Data"
    }
}
EOF

    # FIXME update after default tag logic is defined
    status_code=$(curl --location -g --request POST \
        --url "${LEGAL_HOST}/api/legal/v1/legaltags" \
        --write-out "%{http_code}" --silent --output "output.txt" \
        --header "Content-Type: application/json" \
        --header "Authorization: Bearer ${ID_TOKEN}" \
        --header "data-partition-id: ${DATA_PARTITION_ID}" \
        --data @/opt/default_legal_tag.json)

    if [ "$status_code" == 201 ]; then
        echo "$status_code: Legal tag created successfully!"
    elif [ "$status_code" == 409 ]; then
        cat /opt/output.txt
    else
        cat /opt/output.txt
        exit 1
    fi
    rm /opt/output.txt
}

if [ "${ONPREM_ENABLED}" == "true" ]; then
    source ./validate-env.sh "OPENID_PROVIDER_URL"
    source ./validate-env.sh "OPENID_PROVIDER_CLIENT_ID"
    source ./validate-env.sh "OPENID_PROVIDER_CLIENT_SECRET"

    get_token_onprem

else

    get_token_gc

fi

check_entitlements_readiness

create_legaltag

touch /tmp/bootstrap_ready
