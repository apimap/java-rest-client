Apimap.io REST Client
=====

🥳 **Happy Coding** 🥳

This section is targeted to developers that want to use the REST Client provided by the project.

## Table of Contents

* [Introduction](#introduction)
* [Getting Started](#getting-started)
* [Other Resources](#other-resources)

## Introduction

This is the definition of our REST Client.

| Component | Description |
|---|---|
| RestClient | Code responsible for creating the query stack and responding according to the guidance given in response from the API service |
| RestClientConfiguration | Handle the dynamic properties configured to enable usage of the RestClient |

### RestClient

The main philosophy behind the design is that collections and resources are the object types behind an REST API. These resources can be queried for paths, interesting links to other associated resources and manipulated using REST Commands (POST/PUT/GET/DELETE).

#### Error Handling
| Component | Description |
|---|---|
| withErrorHandler | Adds a callback that is performed when an error occurs in the client |

#### Resource Discovery
| Component | Description |
|---|---|
| followCollection | Follows a item in a collection the the given URL |
| followResource | Follows a relationship defined by a resource |

#### Resource Manipulation
| Component | Description |
|---|---|
| getResource | Get the content of the resource defined by the callstack |
| onMissingCreate | If the resource is not found, create this new resource instead. Enables a daisy chained callstack with multiple create statements nested |
| createResource | Create a new resource at the location defined by the callstack. Fails if a resource is already existing. |
| createOrUpdateResource | If a resource is already existing the new resource will replace the existing |
| deleteResource | Delete the resource defined by the callstack |

### RestClientConfiguration

#### Debug Mode

More extensive logging

#### Dryrun Mode

Does not communicate to any APIs on any actions and returns a default object from create resources.

#### Configuration Options

```java
private String token;
```

The token assosiated with the API that is about to be changed

```java
private String serviceRootEndpointUrl;
```

The root URL to the instance of the service API that this client is to communicate with

```java
private Integer queryCallstackDepth = DEFAULT_CALLSTACK_MAX_DEPTH;
```

The Client builds up a call stack of operations that it then performes. This callstack needs to have a maximum depth in order not possibly fall into a endless loop.

## Getting Started

To view more examples please take a look inside the source code behind the Jenkins plugin and the CLI.

### Create or Update the API Metadata Definition

```java
return RestClient.withConfiguration(configuration)
        .withErrorHandler(errorHandlerCallback)
        .followCollection(JsonApiRootObject.API_COLLECTION)
        .followCollection(metadataDataApiEntity.getName(), JsonApiRootObject.VERSION_COLLECTION)
        .onMissingCreate(metadataDataApiEntity.getName(), apiDataApiEntity, apiCreatedCallback, ApiDataApiEntity.class)
        .followResource(metadataDataApiEntity.getApiVersion())
        .onMissingCreate(metadataDataApiEntity.getApiVersion(), apiVersionDataApiEntity, apiVersionCreatedCallback, ApiVersionDataApiEntity.class)
        .followCollection(JsonApiRootObject.METADATA_COLLECTION)
        .createOrUpdateResource(metadataRootApiEntity, MetadataDataApiEntity.class);
```

## Other Resources
___

- [Hypermedia as the Engine of Application State (HATEOAS) ](https://en.wikipedia.org/wiki/HATEOAS)
- [JSON:API — A specification for building APIs in JSON](https://jsonapi.org/)
