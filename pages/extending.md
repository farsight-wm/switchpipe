---
layout: default
---


 * TOC
  {:toc}


# Integrate jar-Library

If you don't want to use the provided IS package but integrate the functionality with one of your existing packages you need to
 * copy the required jar libraries into your package
 * create the service you want to use and implement it based upon the service from the package `farsightwm.switchpipe`.

# Extending switchpipe

## Custom Sources and Stores
You can implement Sources and Stores by providing a java class that implements one of the interfaces:
 * `farsight.switchpipe.datastore.DataSource`
 * `farsight.switchpipe.datastore.DataStore`

To use a custom store configure a store (e.g. `myStore`) and set:
```
stores.myStore.class = <fully-qualified-name-of-your-class>
```
## Custom Activation Policies

You can implement your own Activation Policy. Implement one of the interfaces:
 * farsight.switchpipe.activation.ActivationPolicy
 * farsight.switchpipe.activation.ConfigurablePolicy
 * farsight.switchpipe.activation.PersistablePolicy

And configure:
```
activation.class = <fully-qualified-name-of-your-class>
```
