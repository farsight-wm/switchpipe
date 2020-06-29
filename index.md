---
layout: home
---
[![License: MIT](https://img.shields.io/badge/License-MIT-silver.svg)](https://opensource.org/licenses/MIT)

Switchpipe stores and restores pipelines based upon debug keys, allowing easy and reproducible debugging.
Switchpipe automatically detects the calling service and generates a debugID based on the current time.
In order to control the amount of pruduced data it is possible to control the behavior of switchpipe on a per service basis.

# Requirements

This software was build for webMethods IntegrationServer 10.5 but should run on other versions as well.

# Installation
Download the latests release as an IntegrationServer zipped IS-Package and install it on your Integration Server. The package contains all required jars and is runnable out of the box.

# Building from Sources

Some jar libraries of webMethods are required in order to build from sources. This steps assume, that there is a version of webMethods installed on the same machine.

1. Clone the repository: farsight-wm/wm-parent
2. Use the provided scripts from wm-parent to install required jars into your local maven repository
3. Clone the repository: farsight-wm/switchpipe
4. Run `mvn clean package`

# Usage

Install the IS package and use the contained service `farsightwm.switchpipe.flow:switchpipe` on every service you want to debug with input variables that are to complex to provide them manually.
By default pipelines are stored in the pipeline directory of your IS instance. You may configure a different directory (see [Configuration](pages/configuration.md)).

1. [BuildInServices](pages/buildInServices.md)
1. [Configuration](pages/configuration.md)

# Advanced Topics

1. [Integrate the package into one of yours](pages/extending.md#integrate-jar-library)
1. [Custom Sources and Stores](pages/extending.md#custom-sources-and-stores)
1. [Custom Activation Policies](pages/extending.md#custom-activation-policies)

## Contribution

 * report bugs
 * write documentation
 * write tests
 * create pull requests
