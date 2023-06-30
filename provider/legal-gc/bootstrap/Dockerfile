FROM google/cloud-sdk:slim

WORKDIR /opt

RUN apt-get update && apt-get install -yq jq

COPY ./provider/legal-gc/bootstrap/ /opt/

RUN chmod +x bootstrap.sh validate-env.sh

RUN groupadd -g 10001 -r nonroot \
  && useradd -d /opt -g 10001 -r -u 10001 nonroot

RUN chown -R 10001:10001 /opt

USER 10001:10001

CMD ["/bin/bash", "-c", "./bootstrap.sh && sleep 365d"]
