package es.jdl.response;

import es.jdl.utils.ExpandedProperties;
import es.jdl.utils.ServletUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class AddHeadersFilter implements Filter {

    private ExpandedProperties config;
    private ServletContext servletContext;
    private Properties headers = new Properties();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config = ServletUtils.buildConfig(filterConfig, this.getClass());
        servletContext = filterConfig.getServletContext();
        String prefixHeader = config.getProperty("prefixHeader", this.getClass().getSimpleName() + ".header.");
        for (String s: config.stringPropertyNames()) {
            if (s.startsWith(prefixHeader))
                headers.setProperty(s.substring(prefixHeader.length()), config.getProperty(s));
        } // for

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        for (String s: headers.stringPropertyNames()) {
            response.addHeader(s, headers.getProperty(s));
        }
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }

}
