package es.jdl.auth;

import es.jdl.utils.ExpandedProperties;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Basic authentication filter. Can be configured with this parameters
 * <ul>
 *     <li>realm: Realm display name</li>
 *     <li>credentialsFile: Configuration file. Could be external file or resource file. Checked in that order</li>
 *     <li>user: For very simple configuration. Just add one user and password</li>
 *     <li>password</li>
 * </ul>
 */
public class BasicAuthenticationFilter implements Filter {

    private String realm = "BasicRealm";
    private ServletContext servletContext;
    private HashMap<String, String> credentials;

    public void init(FilterConfig filterConfig) throws ServletException {
        String paramRealm = filterConfig.getInitParameter("realm");
        if (ExpandedProperties.isStringNotEmpty(paramRealm)) {
            realm = paramRealm;
        }
        servletContext = filterConfig.getServletContext();
        credentials = new HashMap<>();
        String credentialsFile = filterConfig.getInitParameter("credentialsFile");
        if (ExpandedProperties.isStringNotEmpty(credentialsFile)) { // file configured trying to load
            Properties p = new Properties();
            File f = new File(ExpandedProperties.replaceAll(credentialsFile, System.getProperties()));
            URI resource = null;
            if (f.exists()) {
                try {
                    p.load(new FileReader(f));
                } catch (IOException e) {
                    throw new ServletException(e.getMessage() + " reading credentials file " + f, e);
                }
            } else { // file not exists. try classpath
                try {
                    URL r = getClass().getResource(credentialsFile);
                    if (r == null)
                        throw new ServletException("Can't found credentials resource " + credentialsFile);
                    resource = r.toURI();
                    p.load(Files.newInputStream(Paths.get(resource)));
                } catch (URISyntaxException | IOException e) {
                    throw new ServletException(e.getMessage() + " loading credentials resource " + resource, e);
                }
            }
            for (String k: p.stringPropertyNames())
                credentials.put(k, p.getProperty(k));
        } else { // credentialsFile parameter is empty
            String user = filterConfig.getInitParameter("user");
            if (ExpandedProperties.isStringNotEmpty(user))
                credentials.put(user, filterConfig.getInitParameter("password"));
            else
                throw new ServletException("'user' parameter must have value or 'credentialsFile' parameter must be configured");
        }
        filterConfig.getServletContext().log("Loaded " + credentials.keySet().size() + " user(s)");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.getDecoder().decode(st.nextToken().getBytes(StandardCharsets.UTF_8)));
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String _username = credentials.substring(0, p).trim();
                            String _password = credentials.substring(p + 1).trim();

                            if (testCredentials(_username, _password)) {
                                chain.doFilter(servletRequest, servletResponse);
                            } else {
                                unauthorized(response, "Bad credentials");
                            }

                        } else {
                            unauthorized(response, "Invalid authentication token");
                        }
                    } catch (UnsupportedEncodingException e) {
                        servletContext.log(e.getMessage(), e);
                        throw new Error("Couldn't retrieve authentication", e);
                    } catch (IllegalArgumentException e) { // error in base64 encoding
                        servletContext.log(e.getMessage(), e);
                        unauthorized(response, "Invalid authentication token");
                    }
                } else { // TODO: Check this -> not very clean
                    unauthorized(response);
                }
            } else {
                unauthorized(response);
            }
        } else {
            unauthorized(response);
        }
    }

    private boolean testCredentials(String username, String password) {
        return credentials.containsKey(username) && credentials.get(username).equals(password);
    }

    @Override
    public void destroy() {
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        servletContext.log("unauthorized: " + message);
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }
}
