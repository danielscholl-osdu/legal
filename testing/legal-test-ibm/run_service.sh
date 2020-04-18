#!/bin/bash

export TENANT_INFO_DATABASE=acceptance-test-tenant-info
export LEGAL_TAG_DATABASE=acceptance-test-legal-tags
export COUNTRIES_DATABASE=acceptance-test-countries

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

CRED_FILE=$(basename ${IBM_CREDENTIALS_FILE})

mkdir config
cp ${IBM_CREDENTIALS_FILE} config/
cat <<EOF > config/application.yml
ibm:
  tenant:
    cloudant:
      dbName: tenant-info
      credentials: file:config/${CRED_FILE}
  legal:
    cloudant:
      dbName: legal-tags
      credentials: file:config/${CRED_FILE}
EOF

java -jar ${SCRIPT_DIR}/../../provider/legal-ibm/target/legal-ibm-0.0.4-SNAPSHOT-spring-boot.jar
