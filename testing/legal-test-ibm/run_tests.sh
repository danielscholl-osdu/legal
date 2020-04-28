#!/bin/bash

if [ -z "$LEGAL_TEST_TOKEN" ]
then
    echo "env var LEGAL_TEST_TOKEN not set"
    exit 1
fi

DATABASE_PREFIX=acceptance-test
export COUNTRIES_DATABASE=$DATABASE_PREFIX-countries
export TENANT_INFO_DATABASE=$DATABASE_PREFIX-tenant-info
export LEGAL_TAG_DATABASE=$DATABASE_PREFIX-legal-tags
export DATA_PARTITION_ID=data-partition-id
export HOST_URL=http://localhost:8080/api/legal/v1/
#export HOST_URL=http://localhost:8080/api/legal/v1/
export HOST_URL=https://os-legal-ibm-osdu-r2.osduadev-a1c3eaf78a86806e299f5f3f207556f0-0000.us-south.containers.appdomain.cloud/api/legal/v1/
export MY_TENANT=TENANT1
export MY_TENANT_PROJECT=PROJECT1

mvn test
