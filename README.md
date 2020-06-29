# farsight-wm/switchpipe
[![License: MIT](https://img.shields.io/badge/License-MIT-silver.svg)](https://opensource.org/licenses/MIT)

Manage pipeline storage/recovery for debugging and maintenance purposes easily.


## Requirements

Currently tested only with webMethods IntegrationServer 10.5

## Installation
Copy the zipped IS package (see releases) to your IS replicate/inbound directory and install it via the admin UI.

## Usage
The most important service is *farsightwm.switchpipe.flow:switchpipe*. It can be used at the beginning of a service to automatically store the service inputs.
It will generate a xml-encoded pipeline file in the defined output directory (default is IS/instances/CURRENT-INSTANCE/pipelines).
The generated filename contains the name of the calling service and a timestamp. The timestamp may be used as *DebugID* to recover the pipeline.

Thus you can easily debug service with multiple inputs. If you collect pipeline data from other systems (testing, production) you may be able to debug problems in a development environment.

Other service may be used to explicitly save or restore pipeline data or only the content of a document in the pipeline.

## Configuration

See documentation at [github.io](https://farsight-wm.github.io/switchpipe/).