package es.jdl.analytics;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static es.jdl.utils.ExpandedProperties.getDefault;

public class CollectFilter implements Filter {

    private String urlEndpoint;
    private boolean addUserAgent = true;
    private boolean addQueryString = false;
    private long timeOutSeconds = 10;
    private String apiCharset = "UTF-8";
    private String userId = "555"; // anonymous
    private String trackId;
    private int version = 1;
    private String payloadFormatter;

    private HttpClient client;
    private ServletContext servletContext;

    private void log(String message) {
        if (servletContext != null)
            servletContext.log(message);
        else
            System.out.println(message);
    }

    private void log(String message, Throwable cause) {
        if (servletContext != null)
            servletContext.log(message, cause);
        else
            System.out.println(message);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        client = HttpClient.newHttpClient();
        servletContext = filterConfig.getServletContext();
        urlEndpoint = getDefault(filterConfig.getInitParameter("urlEndpoint"), "https://www.google-analytics.com/collect");
        addUserAgent = Boolean.parseBoolean(getDefault(filterConfig.getInitParameter("addUserAgent"), "true"));
        addQueryString = Boolean.parseBoolean(getDefault(filterConfig.getInitParameter("addQueryString"), "false"));
        try {
            timeOutSeconds = Long.parseLong(getDefault(filterConfig.getInitParameter("timeOutSeconds"), "10"));
        } catch (NumberFormatException e) {
            log(filterConfig.getInitParameter("timeOutSeconds") + " is number? " + e.getMessage(), e);
        }
        apiCharset = getDefault(filterConfig.getInitParameter("apiCharset"), "UTF-8");
        trackId = getDefault(filterConfig.getInitParameter("trackId"), null);
        if (trackId == null)
            log("WARN NO trackId CONFIGURED!!. Filter without effect");
        // google example: v=1&tid=UA-123456-1&cid=5555&t=pageview&dp=%2FpageA
        payloadFormatter = getDefault(filterConfig.getInitParameter("payloadFormatter"),
                "v=%d&tid=%s&cid=%s&t=pageview&dp=%s");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // if trackId configured and httpservlet!
        if (trackId != null && servletRequest instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) servletRequest;
            String uri = req.getRequestURI();
            String qs = "";
            if (addQueryString)
                qs = "?" + req.getQueryString();
            HttpRequest request;
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(urlEndpoint))
                .timeout(Duration.ofSeconds(timeOutSeconds));
            if (addUserAgent) {
                builder = builder.header("User-Agent", req.getHeader("User-Agent"));
            }
            String hit = URLEncoder.encode(uri + qs, apiCharset);
            builder = builder.POST(HttpRequest.BodyPublishers.ofString(
                    String.format(payloadFormatter, version, trackId, userId, hit)));
            request = builder.build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> { log(String.valueOf(response.statusCode()));
                        return response; } )
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::log);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // destroy client?
    }
}
