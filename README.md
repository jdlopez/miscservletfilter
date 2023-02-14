[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.jdlopez/miscservletfilter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.jdlopez/miscservletfilter)

# Collection of Servlet Filter

A little collection of useful servlet filters:

* [Google Analytics](analytics.md)
* [Basic Authentication](auth.md)
* [ApiKey Authentication](#apikey-authentication)
* [Response Headers](#addheadersfilter)
* [Block](#block-filter)

## Configuration

First add this to your pom.xml (double check version number with badge)

    <dependency>
        <groupId>io.github.jdlopez</groupId>
        <artifactId>miscservletfilter</artifactId>
        <version>1.0.2</version>
    </dependency>
    
Each filter has its own configuration set. 

Check filter code before use:
* es.jdl.analytics.CollectFilter
* es.jdl.auth.BasicAuthenticationFilter
* es.jdl.auth.HeaderAuthenticationFilter
* es.jdl.response.AddHeadersFilter
* es.jdl.security.BlockingFilter

## Common configuration

All filters can be configured using *configFile* initparam. It can be set with java system properties placeholders.

Ex:

    configFile=${user.home}/myconfig.properties

## ApiKey Authentication

Config content:

    HeaderAuthenticationFilter.key.SOME_KEY=Response returned in header. Could be users name or whatever

Prefix Api Key entries can be changed with _prefixApiKey_ parameter.

## AddHeadersFilter

Config content:

    AddHeadersFilter.header.MY_HEADER=header value

Header can be changed with _prefixHeader_ parameter.

## Block Filter

Config content:

    BlockingFilter.intervalMillis=Max Interval between hits from same IP in millis. Default 1h 
    BlockingFilter.globalMaxRequest=Max number of hits  from same IP in interval. Default 1000
    BlockingFilter.globalMaxSize=IP table max size. To reduce memory allocation

## Deploy to repository

    mvn clean deploy -e -P ossrh 

## News

* 1.1.0
 
Added some mapping utils: [BeanMapper.java](src/main/java/es/jdl/utils/BeanMapper.java)