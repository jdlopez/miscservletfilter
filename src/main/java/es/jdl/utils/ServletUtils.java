package es.jdl.utils;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Some utilities common for Filter clases
 */
public class ServletUtils {

    private static final String PARAM_CONFIG_FILE = "configFile";

    public static final String MIME_JSON = "application/json";

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
            configFile = filterConfig.getServletContext().getInitParameter(aClass.getSimpleName() + "." + PARAM_CONFIG_FILE);
        if (configFile == null) {
            URL r = aClass.getResource("/" + aClass.getSimpleName() + ".properties");
            if (r == null)
                throw new ServletException("Can't load configuration with file nor class resource: /" + aClass.getSimpleName() + ".properties");
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
        if (names != null)
            while (names.hasMoreElements()) {
                String n = names.nextElement();
                ret.setProperty(n, filterConfig.getInitParameter(n));
            } // while
        // not very efficient expanded many times...
        ret.expand(System.getProperties());
        return ret;
    }

    public static void writeJsonToOut(HttpServletResponse response, String json, ServletContext servletContext) {
        response.setContentType(MIME_JSON);
        try {
            response.getWriter().println(json);
            response.getWriter().flush();
        } catch (IOException e) {
            servletContext.log("writing json to out: " + e.getMessage(), e);
        }
    }

    public static Map<String, String> buildMap(String... args) {
        if (args == null || args.length % 2 != 0)
            throw new IllegalArgumentException("Must have arguments even. Actual number: " +
                    (args==null?"null":args.length) );
        HashMap<String, String> ret = new HashMap<>(args.length / 2);
        for (int i = 0; i < args.length; i += 2)
            ret.put(args[i], args[i+1]);
        return ret;
    }

    public static String mapToJsonString(Map<String, String> map) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for (Iterator<String> it = map.keySet().stream().iterator(); it.hasNext(); ) {
            String k = it.next();
            sb.append("\"" + k + "\": \"").append(map.get(k)).append("\"");
            if (it.hasNext())
                sb.append(",");
        } // end for
        sb.append("}");
        return sb.toString();
    }
}
