# Google Analytics Servlet Filter

Servlet filter to add registration of api calls (or any servlet call)
into Google Analytics Measurement Protocol.

It uses async http client call so not much overhead in real api/business call.

## Configuration

First look at [README](README.md) to add maven dependency.

Then if you are using springboot or springmvc, add this to your Config class:

        @Bean
        public FilterRegistrationBean<CollectFilter> analyticsFilter() {
            FilterRegistrationBean<CollectFilter> registrationBean
                    = new FilterRegistrationBean<>();
    
            registrationBean.setFilter(new CollectFilter());
            // Google Analytics property tracking id
            registrationBean.addInitParameter("trackId", System.getenv("trackId"));
            registrationBean.addUrlPatterns("/*");
    
            return registrationBean;
        }

This are filter's configuration init parameters:

* trackId Google Analytics property tracking id (U-XXXXX)
* urlEndpoint only needed if Google changes it
* addUserAgent did u want to pass client user-agent to analytics?
* addQueryString did u want to add query-string (url parameters) to page-view event?
* timeOutSeconds 10 by default, used for HTTP POST
* apiCharset UTF-8 by default, Google said this in its documentation
* userId fixed to anonymous
* payloadFormatter payload printf style string to pass as payload to measurement service: v=%d&tid=%s&cid=%s&t=pageview&dp=%s by default

## Google analytics API:

    https://developers.google.com/analytics/devguides/collection/protocol/v1/reference

## Java JDK11 HTTP async client

    https://openjdk.java.net/groups/net/httpclient/intro.html


