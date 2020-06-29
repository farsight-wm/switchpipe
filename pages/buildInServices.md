---
layout: default
title: BuildIn-Services
---

* TOC
{:toc}

# Configuration

## farsightwm.switchpipe.configuration:checkEnabled
Checks weather the automatic switchpipe triggering is enabled for a given service or not. If it is not enabled, calling the service `farsightwm.switchpipe.flow:switchpipe` without a `debugID` will not do anything.

## farsightwm.switchpipe.configuration:configureService
Configure a service for autotriggering. You may define if a serivce is enabled/disabled or if it shoulg be enabled for a given number of invokes or until a defined date.

## farsightwm.switchpipe.configuration:listServices
Returns a list of configured services along with their configuration.

## farsightwm.switchpipe.configuration:reloadConfiguration
If a configuration file is used, this service reloads the configuration file and ajusts the settings according to the current setup from the file.

## farsightwm.switchpipe.configuration:showConfiguration
Returns the current configuration

# Switching pipelines
Services in this section normally use this set of input parameters:
 * `debugID` (optional)
   An identifier definig the invocation of a service. This is usually a timestamp, but may be anything that maps to a given pipeline file in the used pipeline store.
   The service `farsightwm.switchpipe.flow:switchpipe` triggers a pipeline write if ths value is not provided. If it is, it triggers a read and restore of a saved pipeline.
  * `serviceID` / `debugServiceID`  (optional)
  Defines a service that is used to read or write a pipeline - usually a qualified service name. ('NSName'). If this value is not provieded the calling services name is used.
 * `storeID` / `debugStoreID` (optional)
 If multiple pipeline stores (directories) are defined this value defines the store to be used. If not provieded the default store is used.

## farsightwm.switchpipe.flow:loadPipelineDocument
Loads a pipeline and returns it as an document

## farsightwm.switchpipe.flow:restorePipeline
Loads a pipeline and replaces the current pipeline with the loaded one.

## farsightwm.switchpipe.flow:storePipeline
Stores the current pipeline.

## farsightwm.switchpipe.flow:storePipelineDocument
Stores a provieded document as a pipeline.

## farsightwm.switchpipe.flow:switchpipe
Automatically stores or restores a pipeline depending of the presenece of the paramter `debugID` in the current pipeline.
If you use this service make sure, that the parameter `debugID` is not set accidentially as you might get unexpected behavior if you restore a pipeline accedentuially!
