---
layout: home
---
[![License: MIT](https://img.shields.io/badge/License-MIT-silver.svg)](https://opensource.org/licenses/MIT)

# Requirements

This software was build for webMethods IntegrationServer 10.5 but should run on other versions as well.

# Installation
Download the latests release as an IntegrationServer zipped IS-Package and install it on your Integration Server. The package contains all required jars and is runnable out of the box.

# Building from Sources

Some jar libraries of webMethods are required in order to build from sources. This steps assume, that there is a version of webMethods installed on the same machine.

1. Clone the repository: farsight-wm/farsight-wm
2. Use the script xzy to install required jars into your local maven repository
3. Clone the repository: farsight-wm/switchpipe
4. Run `mvn clean package`

# Usage

1. Concepts ?! How it works?
1. [BuildInServices](pages/buildInServices.md)
1. [Configuration](pages/configuration.md)

# Advanced Topics

//TODO

1. Extending Functionality
   1. Custom Sources and Stores
   1. Custom Activation Policies

## Contribution

 * report bugs
 * write documentation
 * write tests
 * create pull requests
