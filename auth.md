# Basic Authentication Filter for Java-Servlet

Very basic authentication filter. Suitable for Spring Boot REST services without servlet-engine configuration.

## Configuration

Filter parameters could provide credentials repository
* **realm**: Realm display name
* **credentialsFile**: Credentials file, a properties file with user=pass. Could be external file or resource file. Checked in that order
* **one user-password**: Use **user** and **password** parameters

No encription at all. Use with caution.

## Usage

Add dependency to maven pom.xml:

    <dependency>
        <groupId>io.github.jdlopez</groupId>
        <artifactId>basicauthfilter</artifactId>
        <version>1.0.0</version>
    </dependency>

Add servlet filter and configure it. This is a spring-boot sample:

    @Bean
    public FilterRegistrationBean<es.jdl.auth.BasicAuthenticationFilter> authFilter() {
        FilterRegistrationBean<es.jdl.auth.BasicAuthenticationFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(new es.jdl.auth.BasicAuthenticationFilter());
        registrationBean.addInitParameter("realm", "myapp");
        registrationBean.addInitParameter("user", "sampleuser");
        registrationBean.addInitParameter("password", "easypassword");
        registrationBean.addUrlPatterns("/rest/*");

        return registrationBean;
    }

## Aditional documentation

Basic Authentication specification: https://tools.ietf.org/html/rfc7617

Based in code found at: https://gist.github.com/neolitec/8953607

Tested using mockito:
- https://site.mockito.org/
- https://www.baeldung.com/mockito-verify
