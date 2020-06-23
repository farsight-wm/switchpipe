# farsight-wm/switchpipe
[![License: MIT](https://img.shields.io/badge/License-MIT-silver.svg)](https://opensource.org/licenses/MIT)
## Installation
Download the latests release as an IntegrationServer zipped IS-Package and install it on your Integration Server. The package contains all required jars and is runnable out of the box.

## BuiltIn-Services
### Configuration
#### farsightwm.switchpipe.configuration:checkEnabled
Checks weather the automatic switchpipe triggering is enabled for a given service or not. If it is not enabled, calling the service `farsightwm.switchpipe.flow:switchpipe` without a `debugID` will not do anything.
#### farsightwm.switchpipe.configuration:configureService
Configure a service for autotriggering. You may define if a serivce is enabled/disabled or if it shoulg be enabled for a given number of invokes or until a defined date.
#### farsightwm.switchpipe.configuration:listServices
Returns a list of configured services along with their configuration.
#### farsightwm.switchpipe.configuration:reloadConfiguration
If a configuration file is used, this service reloads the configuration file and ajusts the settings according to the current setup from the file.
#### farsightwm.switchpipe.configuration:showConfiguration
Returns the current configuration
### Switching pipelines
Services in this section normally use this set of input parameters:
 * `debugID` (optional)
   An identifier definig the invocation of a service. This is usually a timestamp, but may be anything that maps to a given pipeline file in the used pipeline store.
   The service `farsightwm.switchpipe.flow:switchpipe` triggers a pipeline write if ths value is not provided. If it is, it triggers a read and restore of a saved pipeline.
  * `serviceID` / `debugServiceID`  (optional)
  Defines a service that is used to read or write a pipeline - usually a qualified service name. ('NSName'). If this value is not provieded the calling services name is used.
 * `storeID` / `debugStoreID` (optional)
 If multiple pipeline stores (directories) are defined this value defines the store to be used. If not provieded the default store is used.

#### farsightwm.switchpipe.flow:loadPipelineDocument
Loads a pipeline and returns it as an document
#### farsightwm.switchpipe.flow:restorePipeline
Loads a pipeline and replaces the current pipeline with the loaded one.
#### farsightwm.switchpipe.flow:storePipeline
Stores the current pipeline.
#### farsightwm.switchpipe.flow:storePipelineDocument
Stores a provieded document as a pipeline.
#### farsightwm.switchpipe.flow:switchpipe
Automatically stores or restores a pipeline depending of the presenece of the paramter `debugID` in the current pipeline.
If you use this service make sure, that the parameter `debugID` is not set accidentially as you might get unexpected behavior if you restore a pipeline accedentuially!

## Configuration
If you want more control of the behavior of switchpipe, you can create a configration file in the config-directory of the current instance (e.g. `.../IntegrationServer/instances/default/configuration/switchpipe.conf`).
The format is like java properties files: `some.key = some value`
Comments may be used and must start at every line with a `#`-sign.
The following keys are supported:
|key|description|
|--|--|
| switchEnabled | Globaly enabled or disable switchpipe (autoswitching). Must be one of {`true`, `false`}. Default is `true`. |
| activation.strategy | Defines a stratigy to be used with services, that are not explicitly defined (see: `farsightwm.switchpipe.configuration:configureService`) |
| activation.class | ClassName of the activation policy. Switchpipe supplies some policies, the (default) is `NsHintPolicy` implementation - see: 'Activation Policies'. You may implement your own policy and set a full qualifed class name here (e.g. `my.package.switchpipe.CustomActicationPolicy`) |
| activation.class.{custom} | Custom parameter passed to the activation policy class. |
| pipelineCodec.class | ClassName of the pipeline Codec. Switchpipe supplies the (default) `DefaultPipelineCodec` implementation. You may implement your own Codec and set a full qualifed class name here (e.g. `my.package.switchpipe.CustomPipelineCodec`) |
| pipelineCodec.class.maxSize | The `DefaultPipelineCodec` supports this setting. If it is set, pipelines only get stored if the output file size is less than this value. You may use `k` or `m` to switch from bytes to kB or MB. |
| pipelineCodec.class.{custom} | Custom parameter passed to the pipeline codec class. |
| store.defaults... | see 'Define a store' |
| stores.{storeId}... | see 'Define a store' |

### Define a store
A store for switchpipe is a container that can read and store files within a path. Stores can be readonly (then it's refered to as 'source'). This may be simply a directory. But it is also possible to load data from network or a zip-file.

You can implemt your own stores/sources if needed.

You may define as much stores to be used as you need/like. To ease the definition of a store you may define defaults that will be used if a key is not defined for a store explicitly.
The defaults must use the prefix `store.defaults.` Each store is defined by suppliing values with a prefix of `stores.{storeID}.` (without parenthesis).
The first store defined gets to be the default store that is used if no store is selected explicitly. You also can set the default store manually by suppling:
`store.defaultStore = {storeID}`

The following keys are used for stores/sources (or the defaults)
|key|description|
|--|--|
| base | A directory that is used as root (default is `pipeline`) |
| pattern | A pattern that is used to map services / invokes to filenames. (default is `${serviceId}/${invokeID}` |
| timestampPattern | A SimpleDatePattern that is used to generate invokeIDs. (default is `yyMMdd-HHmmssSSS`) |
| fallback | A storeID that is to be queried on data load, if the current stores has no data for the given ids. |
| class | A classname of the store implementation |
| class.{custom} | Custom parameters that are passed to the store. |

Currently the possible values for class parameter of a store are:
 * `FileDataStore`
 Writeable data store.
 * `FileDataSource`
 Readonly data source.
 * `ZipDataSource`
 Readonly data source that reads zip files. (Needs custom arguments //TODO)

You may implement your own class and set a fully qualified class name instead.

## Advanced Topics

//TODO

### Activation Policies

### Build from sources

### Extending Functionality

#### Custom Sources

#### Custom Activation Policies

## Contribution

 * report bugs
 * write documentation
 * write tests
 * create pull requests
