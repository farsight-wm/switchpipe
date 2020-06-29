---
layout: default
title: Configuration
---

* TOC
{:toc}

# Configuration
If you want more control of the behavior of switchpipe, you can create a configration file in the config-directory of the current instance (e.g. `.../IntegrationServer/instances/default/configuration/switchpipe.conf`).
The format is like java properties files: `some.key = some value`
Comments may be used and must start at every line with a `#`-sign.
The following keys are supported:

|key|description|
|--|--|
| `switchEnabled` | Globaly enabled or disable switchpipe (autoswitching). Must be one of {`true`, `false`}. Default is `true`. |
| `activation.strategy` | Defines a stratigy to be used with services, that are not explicitly defined (see: `farsightwm.switchpipe.configuration:configureService`) |
| `activation.class` | ClassName of the activation policy. Switchpipe supplies some policies, the (default) is `NsHintPolicy` implementation - see: 'Activation Policies'. You may implement your own policy and set a full qualifed class name here (e.g. `my.package.switchpipe.CustomActicationPolicy`) |
| `pipelineCodec.class` | ClassName of the pipeline Codec. Switchpipe supplies the (default) `DefaultPipelineCodec` implementation. You may implement your own Codec and set a full qualifed class name here (e.g. `my.package.switchpipe.CustomPipelineCodec`) |
| `activation.class.{custom}` | Custom parameter passed to the activation policy class. |
| `pipelineCodec.class.maxSize` | The `DefaultPipelineCodec` supports this setting. If it is set, pipelines only get stored if the output file size is less than this value. You may use `k` or `m` to switch from bytes to kB or MB. |
| `pipelineCodec.class.{custom}` | Custom parameter passed to the pipeline codec class. |
| `store.defaults...` | see 'Define a store' |
| `stores.{storeId}...` | see 'Define a store' |

## Define a store
A store for switchpipe is a container that can read and store files within a path. Stores can be readonly (then it's refered to as 'source'). This may be simply a directory. But it is also possible to load data from network or a zip-file.

You can implemt your own stores/sources if needed.

You may define as much stores to be used as you need/like. To ease the definition of a store you may define defaults that will be used if a key is not defined for a store explicitly.
The defaults must use the prefix `store.defaults.` Each store is defined by suppliing values with a prefix of `stores.{storeID}.` (without parenthesis).
The first store defined gets to be the default store that is used if no store is selected explicitly. You also can set the default store manually by suppling:
`store.defaultStore = {storeID}`

The following keys are used for stores/sources (or the defaults)

|key|description|
|--|--|
| `base` | A directory that is used as root (default is `pipeline`) |
| `pattern` | A pattern that is used to map services / invokes to filenames. (default is `${serviceId}/${invokeID}` |
| `timestampPattern` | A SimpleDatePattern that is used to generate invokeIDs. (default is `yyMMdd-HHmmssSSS`) |
| `fallback` | A storeID that is to be queried on data load, if the current stores has no data for the given ids. |
| `class` | A classname of the store implementation |
| `class.{custom}` | Custom parameters that are passed to the store. |

Currently the possible values for class parameter of a store are:
 * `FileDataStore`
 Writeable data store.
 * `FileDataSource`
 Readonly data source.
 * `ZipDataSource`
 Readonly data source that reads zip files. (Needs custom arguments //TODO)

You may implement your own class and set a fully qualified class name instead.
