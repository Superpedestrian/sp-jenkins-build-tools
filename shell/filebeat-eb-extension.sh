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
###################### Filebeat Configuration Example #########################

# This file is an example configuration file highlighting only the most common
# options. The filebeat.full.yml file from the same directory contains all the
# supported options with more comments. You can use it as a reference.
#
# You can find the full configuration reference here:
# https://www.elastic.co/guide/en/beats/filebeat/index.html

#=========================== Filebeat prospectors =============================

filebeat.prospectors:

# Each - is a prospector. Most options can be set at the prospector level, so
# you can use different prospectors for various configurations.
# Below are the prospector specific configurations.

- input_type: log
  paths: 
    - /var/log/*.log
    - /var/log/syslog
  fields:
    document_type: "syslog"
  fields_under_root: true 

- input_type: log
  paths: 
    - /var/log/nginx/*.log
  fields:
    document_type: "nginx"
  fields_under_root: true

- input_type: log
  paths: 
    - /var/log/apache*/*
  fields:
    document_type: "apache"
  fields_under_root: true

  # Exclude lines. A list of regular expressions to match. It drops the lines that are
  # matching any regular expression from the list.
  #exclude_lines: ["^DBG"]

  # Include lines. A list of regular expressions to match. It exports the lines that are
  # matching any regular expression from the list.
  #include_lines: ["^ERR", "^WARN"]

  # Exclude files. A list of regular expressions to match. Filebeat drops the files that
  # are matching any regular expression from the list. By default, no files are dropped.
  #exclude_files: [".gz$"]

  # Optional additional fields. These field can be freely picked
  # to add additional information to the crawled log files for filtering
  #fields:
  #  level: debug
  #  review: 1

  ### Multiline options

  # Mutiline can be used for log messages spanning multiple lines. This is common
  # for Java Stack Traces or C-Line Continuation

  # The regexp Pattern that has to be matched. The example pattern matches all lines starting with [
  #multiline.pattern: ^\[

  # Defines if the pattern set under pattern should be negated or not. Default is false.
  #multiline.negate: false

  # Match can be set to "after" or "before". It is used to define if lines should be append to a pattern
  # that was (not) matched before or after or as long as a pattern is not matched based on negate.
  # Note: After is the equivalent to previous and before is the equivalent to to next in Logstash
  #multiline.match: after


#================================ General =====================================

# The name of the shipper that publishes the network data. It can be used to group
# all the transactions sent by a single shipper in the web interface.
#name:

# The tags of the shipper are included in their own field with each
# transaction published.
#tags: ["service-X", "web-tier"]

# Optional fields that you can specify to add additional information to the
# output.
#fields:
#  env: staging

#================================ Outputs =====================================

# Configure what outputs to use when sending the data collected by the beat.
# Multiple outputs may be used.

#-------------------------- Elasticsearch output ------------------------------
#output.elasticsearch:
  # Array of hosts to connect to.
  #hosts: ["localhost:9200"]

  # Optional protocol and basic auth credentials.
  #protocol: "https"
  #username: "elastic"
  #password: "changeme"

#----------------------------- Logstash output --------------------------------
output.logstash:
  # The Logstash hosts
  hosts: ["logs.internal.superpedestrian.com:5044"]

  # Optional SSL. By default is off.
  # List of root certificates for HTTPS server verifications
  ssl.certificate_authorities: "/etc/ssl/certs/logstash-ca.pem"

  # Certificate for SSL client authentication
  ssl.certificate: "/etc/ssl/certs/logstash-client.pem"

  # Client Certificate Key
  ssl.key: "/etc/ssl/logstash-client.key"

#================================ Logging =====================================

# Sets log level. The default log level is info.
# Available log levels are: critical, error, warning, info, debug
logging.level: debug

# At debug level, you can selectively enable logging only for some components.
# To enable all selectors use ["*"]. Examples of other selectors are "beat",
# "publish", "service".
#logging.selectors: ["*"]

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
    command: "curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-5.6.5-x86_64.rpm"
    test: "[ ! -f filebeat-${FB_VERSION}-x86_64.rpm ]"
    cwd: "/home/ec2-user"
  200_command:
    command: "rpm -ivh --replacepkgs filebeat-${FB_VERSION}-x86_64.rpm && touch /tmp/installed-filebeats"
    test: "[ ! -f /tmp/installed-filebeats ]"
    cwd: "/home/ec2-user"
  300_command:
     command: "/etc/init.d/filebeat start"
EOF
