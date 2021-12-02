[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.jdlopez/miscservletfilter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.jdlopez/miscservletfilter)

# Collection of Servlet Filter

A little collection of useful servlet filters:

* [Google Analytics](analytics.md)
* [Basic Authentication](auth.md)
* [ApiKey Authentication](#apikey-authentication)
* [Response Headers](#addheadersfilter)

## Configuration

First add this to your pom.xml

    <dependency>
        <groupId>io.github.jdlopez</groupId>
        <artifactId>miscservletfilter</artifactId>
        <version>1.0.1</version>
    </dependency>
    
Each filter has its own configuration set. 

Check filter code before use:
* es.jdl.analytics.CollectFilter
* es.jdl.auth.BasicAuthenticationFilter
* es.jdl.auth.HeaderAuthenticationFilter
* es.jdl.response.AddHeadersFilter

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

## Deploy to repository

    mvn clean deploy -e -P ossrh 