package es.jdl.auth;

import es.jdl.utils.ExpandedProperties;
import es.jdl.utils.ServletUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HeaderAuthenticationFilter implements Filter {

    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String API_KEY_HEADER_RESPONSE = "X-API-Key-Response";

    private ExpandedProperties config = null;
    private ServletContext servletContext;
    private String prefixApiKey;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
         config = ServletUtils.buildConfig(filterConfig, this.getClass());
         servletContext = filterConfig.getServletContext();
         prefixApiKey = config.getProperty("prefixApiKey", this.getClass().getSimpleName() + ".key.");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String authHeader = request.getHeader(API_KEY_HEADER);
        String authValue = config.getProperty(prefixApiKey + authHeader);
        if (authValue != null) { // OK
            response.addHeader(API_KEY_HEADER_RESPONSE, authValue);
            chain.doFilter(servletRequest, servletResponse);
        } else {
            unauthorized(response);
        }

    }

    @Override
    public void destroy() {

    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        servletContext.log("unauthorized: " + message);
        //response.setHeader("WWW-Authenticate", "HEADER " + API_KEY_HEADER);
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }
}
