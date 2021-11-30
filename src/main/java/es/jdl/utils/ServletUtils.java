package es.jdl.utils;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Some utilities common for Filter clases
 */
public class ServletUtils {

    private static final String PARAM_CONFIG_FILE = "configFile";

    /**
     * Load config properties using  {@link #PARAM_CONFIG_FILE} as initParam placeholder for external file or internal resource.
     * Also
     */
    public static ExpandedProperties buildConfig(FilterConfig filterConfig, Class aClass) throws ServletException {
        if (aClass == null || filterConfig == null)
            throw new ServletException("filterConfig (" + filterConfig + ") and class (" + aClass + ") must be provided");
        ExpandedProperties ret = null;
        String configFile = filterConfig.getInitParameter(PARAM_CONFIG_FILE);
        if (configFile == null)
            configFile = filterConfig.getServletContext().getInitParameter(aClass.getName() + "." + PARAM_CONFIG_FILE);
        if (configFile == null) {
            URL r = aClass.getResource("/" + aClass.getName() + ".properties");
            if (r == null)
                throw new ServletException("Can't load configuration with file nor class resource: /" + aClass.getName() + ".properties");
            else {
                try {
                    ret = new ExpandedProperties(Files.newInputStream(Paths.get(r.toURI())));
                } catch (URISyntaxException | IOException e) {
                    throw new ServletException(e.getMessage() + " loading resource " + r, e);
                }
            }
        } else {
            try {
                Path path = Paths.get( ExpandedProperties.replaceAll(configFile, System.getProperties()) );
                if (!path.toFile().exists()) { // try resource
                    URL r = aClass.getResource(configFile);
                    if (r == null)
                        throw new ServletException("Can't load configuration with class resource: " + configFile);
                    else {
                        try {
                            ret = new ExpandedProperties(Files.newInputStream(Paths.get(r.toURI())));
                        } catch (URISyntaxException | IOException e) {
                            throw new ServletException(e.getMessage() + " loading resource " + r, e);
                        }
                    }
                } else // path existe
                    ret = new ExpandedProperties(Files.newInputStream(path));
            } catch (IOException e) {
                throw new ServletException(e.getMessage() + " loading file " + configFile, e);
            }
        }
        if (ret == null) { // if reaches here there is no config file whatsoever
            ret = new ExpandedProperties(new Properties());
        }
        Enumeration<String> names = filterConfig.getInitParameterNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            ret.setProperty(n, filterConfig.getInitParameter(n));
        } // while
        // not very efficient expanded many times...
        ret.expand(System.getProperties());
        return ret;
    }
}
