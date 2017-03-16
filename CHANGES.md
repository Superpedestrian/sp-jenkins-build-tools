# Changes

## 0.8.1

- Changed timout to 10 minutes to allow for docker container
  builds/downloads causing infinite downtime.

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
