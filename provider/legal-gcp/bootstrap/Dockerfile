FROM google/cloud-sdk:slim

WORKDIR /opt

RUN apt-get update && apt-get install -yq jq

COPY ./provider/legal-gcp/bootstrap/ /opt/

RUN chmod +x bootstrap.sh validate-env.sh

CMD ["/bin/bash", "-c", "./bootstrap.sh && sleep 365d"]
