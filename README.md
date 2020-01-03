# Basic Authentication Filter for Java-Servlet

Very basic authentication filter. Suitable for Spring Boot REST services without servlet-engine configuration.

## Configuration

Filter parameters could provide credentials repository 
 * **realm**: Realm display name
 * **credentialsFile**: Credentials file, a properties file with user=pass. Could be external file or resource file. Checked in that order
 * **one user-password**: Use **user** and **password** parameters
 
No encription at all. Use with caution.

## Documentation

Basic Authentication specification: https://tools.ietf.org/html/rfc7617

Based in code found at: https://gist.github.com/neolitec/8953607

Tested using mockito: 
- https://site.mockito.org/
- https://www.baeldung.com/mockito-verify

## Deploy to repository

    mvn clean deploy -e -P ossrh