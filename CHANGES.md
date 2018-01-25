# Changes

## 0.12.3

- Simplify filebeat config and get it deployable

## 0.12.2

- Changed filebeat config to pull filebeat.yml from S3

## 0.12.1

- Fixed filename of filebeat config

## 0.12.0

- Changed filebeat configuration to match upgraded version

## 0.11.0

- Updated logstash hosts

## 0.10.0

- Added command for getting composer version
- Specify env swap for apps that more than 2 environments

## 0.9.0

- Added a DEBUG environment flag to help with shell script/environment issues
- Corrected double deployment bug when using extensions

## 0.8.1

- Changed ELB healtcheck timeout to 10 minutes to prevent infinite downtime

## 0.8.0

- Added support for using ELB health checks for autoscaling instead of EC2

## 0.7.0

- Added command for finding active EB environment by CNAME
- Added command for cloning an EB environment
- Added command for terminating an EB environment

## 0.6.1

- Removed deprecated -f argument docker tag

## 0.6.0

- Added windows support for git version tagging

## 0.5.0

- Updated Elastic Beanstalk deployment script to support extensions

## 0.4.0

- Added optional filebeat elastic beanstalk extension
