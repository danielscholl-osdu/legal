#!/bin/bash

BASE_URL=$(jq -r ".url" < ${IBM_CREDENTIALS_FILE})
USERNAME=$(jq -r ".username" < ${IBM_CREDENTIALS_FILE})
PASSWORD=$(jq -r ".password" < ${IBM_CREDENTIALS_FILE})

DATABASE_PREFIX=acceptance-test

curl -XPUT -u $USERNAME:$PASSWORD $BASE_URL/$DATABASE_PREFIX-countries
curl -XPUT -u $USERNAME:$PASSWORD $BASE_URL/$DATABASE_PREFIX-legal-tags
curl -XPUT -u $USERNAME:$PASSWORD $BASE_URL/$DATABASE_PREFIX-tenant-info

# snagged from: https://stackoverflow.com/a/51264222/26510
function toAbsPath {
    local target
    target="$1"

    if [ "$target" == "." ]; then
        echo "$(pwd)"
    elif [ "$target" == ".." ]; then
        echo "$(dirname "$(pwd)")"
    else
        echo "$(cd "$(dirname "$1")"; pwd)/$(basename "$1")"
    fi
}

function getScriptDir(){
  local SOURCED
  local RESULT
  (return 0 2>/dev/null) && SOURCED=1 || SOURCED=0

  if [ "$SOURCED" == "1" ]
  then
    RESULT=$(dirname "$1")
  else
    RESULT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
  fi
  toAbsPath "$RESULT"
}

SCRIPT_DIR=$(getScriptDir "$0")



TEST_COUNTRIES=${SCRIPT_DIR}/../legal-test-core/src/main/resources/TenantConfigTestingPurpose.json
cat $TEST_COUNTRIES | jq -rc '{ "docs": [.[] + {"tenant": "TENANT1", "region": "us"}]}' | curl -XPOST --data-binary @- -H"Content-type: application/json" -u $USERNAME:$PASSWORD $BASE_URL/$DATABASE_PREFIX-countries/_bulk_docs

TEST_TENANT=${SCRIPT_DIR}/src/test/resources/Tenant.json
curl -XPOST --data-binary @${TEST_TENANT} -H"Content-type: application/json" -u $USERNAME:$PASSWORD $BASE_URL/$DATABASE_PREFIX-tenant-info/_bulk_docs

TEST_TAGS=${SCRIPT_DIR}/src/test/resources/InitialTags.json
curl -XPOST --data-binary @${TEST_TAGS} -H"Content-type: application/json" -u $USERNAME:$PASSWORD $BASE_URL/$DATABASE_PREFIX-legal-tags/_bulk_docs
