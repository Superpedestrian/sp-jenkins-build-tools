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

LOGSTASH_DOC_TYPE=${LOGSTASH_DOC_TYPE:elasticbeanstalk}
FB_EXTENSION_PATH=${FB_EXTENSION_PATH:-.ebextensions/100-filebeat.config}
FB_VERSION=${FB_VERSION:-5.6.5}

indent_cert() {
    # shellcheck disable=SC2001
    echo "$1" | sed 's/^/        /'
}

for var in LOGSTASH_CLIENT_KEY LOGSTASH_CLIENT_CERT LOGSTASH_CA_CERT; do
    if [ -z "${!var}" ]; then
       echo "$var is not set, not running filebeat extension"
       exit 1
    fi
done

cat << EOF > "$FB_EXTENSION_PATH"
files:
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
packages:
  rpm:
    filebeat: https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-5.6.5-x86_64.rpm
commands:
  100_command:
    command: "rm -rf /etc/filebeat"
  200_command:
    command: "aws s3 cp s3://ebext-config/filebeat/filebeat.yml /tmp"
    test: "[ ! -f /tmp/filebeat.yml ]"
    cwd: "/tmp"
  400_command:
    command: "mkdir /etc/filebeat"
    test: "[ ! -f /etc/filebeat/ ]"
    cwd: "/home/ec2-user"
  500_command:
    command: "mv /tmp/filebeat.yml /etc/filebeat/filebeat.yml"
    test: "[ ! -f /etc/filebeat/filebeat.yml ]"
  600_command:
    command: "export LOGSTASH_DOC_TYPE='$LOGSTASH_DOC_TYPE'"
  700_command:
    command: "/etc/init.d/filebeat start"
EOF
