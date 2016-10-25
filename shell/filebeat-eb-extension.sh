#!/bin/bash
# Creates a file at specified location by variable $filebeatextension
#
# Required variables:
# LOGSTASH_CLIENT_KEY: SSL key to authenticate to logstash server
# LOGSTASH_CLIENT_CERT: SSL certificate in PEM format that matches key
# LOGSTASH_CA_CERT: SSL certificate of certificate authority logstash is using
#
# Optional variables:
# LOGSTASH_DOC_TYPE: Log type tagging from filebeat to logstash
# FB_EXTENSION_PATH: Path to filebeat extension configuration to generate
# FB_VERSION: Version number of filebeat to install

LOGSTASH_DOC_TYPE=${LOGSTASH_DOC_TYPE:-elasticbeanstalk}
FB_EXTENSION_PATH=${FB_EXTENSION_PATH:-.ebextensions/100-filebeat.config}
FB_VERSION=${FB_VERSION:-1.2.3}

indent_cert() {
    # shellcheck disable=SC2001
    echo "$1" | sed 's/^/        /'
}

for var in LOGSTASH_CLIENT_KEY LOGSTASH_CLIENT_CERT LOGSTASH_CA_CERT; do
    if [ -z "${!var}" ]; then
       echo "$var is not set, not running filebeat extension"
       exit 0
    fi
done

cat << EOF > "$FB_EXTENSION_PATH"
files:
    "/etc/filebeat/filebeat.yml":
        mode: "0644"
        owner: root
        group: root
        content: |
          filebeat:
            prospectors:
              - paths:
                  - "/var/log/containers/*.log"
                document_type: "$LOGSTASH_DOC_TYPE"

          output:
            logstash:
              hosts: ["logs.internal.superpedestrian.com:52001"]
              tls:
                certificate_authorities: ["/etc/ssl/certs/logstash-ca.pem"]
                certificate: "/etc/ssl/certs/logstash-client.pem"
                certificate_key: "/etc/ssl/logstash-client.key"
    "/etc/ssl/certs/logstash-ca.pem":
      mode: "0644"
      owner: root
      group: root
      content: |
$(indent_cert "$LOGSTASH_CA_CERT")

    "/etc/ssl/certs/logstash-client.pem":
      mode: "0644"
      owner: root
      group: root
      content: |
$(indent_cert "$LOGSTASH_CLIENT_CERT")

    "/etc/ssl/logstash-client.key":
      mode: "0600"
      owner: root
      group: root
      content: |
$(indent_cert "$LOGSTASH_CLIENT_KEY")

commands:
  100_command:
    command: "curl -L -O https://download.elastic.co/beats/filebeat/filebeat-1.2.3-x86_64.rpm"
    test: "[ ! -f filebeat-${FB_VERSION}-x86_64.rpm ]"
    cwd: "/home/ec2-user"
  200_command:
    command: "rpm -ivh --replacepkgs filebeat-${FB_VERSION}-x86_64.rpm && touch /tmp/installed-filebeats"
    test: "[ ! -f /tmp/installed-filebeats ]"
    cwd: "/home/ec2-user"
  300_command:
     command: "/etc/init.d/filebeat start"
EOF
