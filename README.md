![Logo](https://s3-eu-west-1.amazonaws.com/org.paraio/para.png)
============================

> ### Hazelcast Cache plugin for Para

[![Build Status](https://travis-ci.org/Erudika/para-cache-hazelcast.svg?branch=master)](https://travis-ci.org/Erudika/para-cache-hazelcast)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.erudika/para-cache-hazelcast/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.erudika/para-cache-hazelcast)
[![Join the chat at https://gitter.im/Erudika/para](https://badges.gitter.im/Erudika/para.svg)](https://gitter.im/Erudika/para?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## What is this?

**Para** was designed as a simple and modular back-end framework for object persistence and retrieval.
It enables your application to store objects directly to a data store (NoSQL) or any relational database (RDBMS)
and it also automatically indexes those objects and makes them searchable.

This plugin allows Para to cache objects in Hazelcast.

## Documentation

### [Read the Docs](https://paraio.org/docs)

## Getting started

The plugin is on Maven Central. Here's the Maven snippet to include in your `pom.xml`:

```xml
<dependency>
  <groupId>com.erudika</groupId>
  <artifactId>para-cache-hazelcast</artifactId>
  <version>{see_green_version_badge_above}</version>
</dependency>
```

Alternatively you can download the JAR from the "Releases" tab above put it in a `lib` folder alongside the server
WAR file `para-x.y.z.war`. Para will look for plugins inside `lib` and pick up the Hazelcast plugin.

### Configuration

Here are all the configuration properties for this plugin (these go inside your `application.conf`):
```ini
para.hc.async_enabled	= false.
para.hc.eviction_policy	= "LRU".
para.hc.eviction_percentage	= 25.
para.hc.ttl_seconds	= 3600
para.hc.max_size = 25
para.hc.jmx_enabled	= true.
para.hc.ec2_discovery_enabled	= true.
para.hc.discovery_group	= "hazelcast"
para.hc.password = "hcpasswd".
para.hc.mancenter_enabled	= false.
para.hc.mancenter_url	= "http://localhost:8001/mancenter"
```

Finally, set the config property:
```
para.cache = "HazelcastCache"
```
This could be a Java system property or part of a `application.conf` file on the classpath.
This tells Para to use the Hazelcast cache implementation instead of the default one.

### Requirements

- Hazelcast
- [Para Core](https://github.com/Erudika/para)

## License
[Apache 2.0](LICENSE)
