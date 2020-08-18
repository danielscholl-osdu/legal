# This script executes the test and copies reports to the provided output directory
# To call this script from the service working directory
# ./dist/testing/integration/build-aws/run-tests.sh "./reports/"


SCRIPT_SOURCE_DIR=$(dirname "$0")
echo "Script source location"
echo "$SCRIPT_SOURCE_DIR"
(cd "$SCRIPT_SOURCE_DIR"/../bin && ./install-deps.sh)

#### ADD REQUIRED ENVIRONMENT VARIABLES HERE ###############################################
# The following variables are automatically populated from the environment during integration testing
# see os-deploy-aws/build-aws/integration-test-env-variables.py for an updated list

### DYNAMIC PARMETERS ###
# AWS_COGNITO_CLIENT_ID
# ELASTIC_HOST
# ELASTIC_PORT
# FILE_URL
# LEGAL_URL
# RESOURCE_PREFIX
# SEARCH_URL
# LEGAL_QUEUE
# LEGAL_S3_BUCKET

### STATIC PARAMETERS ###: KEEP IN ALPHABETICAL ORDER 
# ** DO NOT ADD VARIABLES NOT USED BY THIS SERVICE!!!! **
export AWS_COGNITO_AUTH_FLOW=USER_PASSWORD_AUTH
export AWS_COGNITO_AUTH_PARAMS_PASSWORD=$ADMIN_PASSWORD
export AWS_COGNITO_AUTH_PARAMS_USER=$ADMIN_USER
export AWS_COGNITO_CLIENT_ID=$AWS_COGNITO_CLIENT_ID
export AWS_S3_ENDPOINT=s3.us-east-1.amazonaws.com
export AWS_S3_REGION=us-east-1
export DYNAMO_DB_ENDPOINT=dynamodb.us-east-1.amazonaws.com
export DYNAMO_DB_REGION=us-east-1
export HOST_URL=$LEGAL_URL
export MY_TENANT=int-test-legal
export S3_LEGAL_CONFIG_BUCKET=$LEGAL_S3_BUCKET
export SKIP_HTTP_TESTS=true
export TABLE_PREFIX=$RESOURCE_PREFIX


#### RUN INTEGRATION TEST #########################################################################

mvn test -f "$SCRIPT_SOURCE_DIR"/../pom.xml
TEST_EXIT_CODE=$?

#### COPY TEST REPORTS #########################################################################

if [ -n "$1" ]
  then
    mkdir -p "$1"
    cp -R "$SCRIPT_SOURCE_DIR"/../target/surefire-reports "$1"
fi

exit $TEST_EXIT_CODE