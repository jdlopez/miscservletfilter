package es.jdl.security;

import es.jdl.utils.ExpandedProperties;
import es.jdl.utils.ServletUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class BlockingFilter implements Filter {

    public static final int HTTP_TOO_MANY_REQUEST = 429;

    private int globalMaxRequest = 1000;
    private long intervalMillis = 3600 * 1000; //  = 1h. Time interval between hits
    private int globalMaxSize = 1000;
    private String uriAdminPrefix = "/admin/block/";

    private ExpandedProperties config;
    private ServletContext servletContext;

    private Hashtable<Integer, BlockInfo> blocks = new Hashtable<>();

    private String blockConfigPrefix = this.getClass().getSimpleName() +  ".block.";
    private String ipHeaderConfigPrefix = this.getClass().getSimpleName() +  ".ip_header.";

    private List<String> IP_HEADERS =
            Arrays.asList("X-Forwarded-For",
                    "Proxy-Client-IP",
                    "WL-Proxy-Client-IP",
                    "HTTP_CLIENT_IP",
                    "HTTP_X_FORWARDED_FOR");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String urlsPrefix = this.getClass().getSimpleName() + ".uriAdminPrefix";
        String maxPrefix = this.getClass().getSimpleName() + ".globalMaxRequest";
        String intervalPrefix = this.getClass().getSimpleName() + ".intervalMillis";
        String maxSizePrefix = this.getClass().getSimpleName() + ".globalMaxSize";

        config = ServletUtils.buildConfig(filterConfig, this.getClass());
        servletContext = filterConfig.getServletContext();
        for (String entry: config.stringPropertyNames()) {
            if (entry.equalsIgnoreCase(urlsPrefix))
                this.uriAdminPrefix = config.getProperty(entry);
            else if (entry.equalsIgnoreCase(maxPrefix))
                this.globalMaxRequest = Integer.parseInt(config.getProperty(entry));
            else if (entry.equalsIgnoreCase(intervalPrefix))
                this.intervalMillis = Long.parseLong(config.getProperty(entry));
            else if (entry.equalsIgnoreCase(maxSizePrefix))
                this.globalMaxSize = Integer.parseInt(config.getProperty(entry));
            else if (entry.startsWith(ipHeaderConfigPrefix))
                IP_HEADERS.add(config.getProperty(entry));
            // now check blocking IPs config
            else if (entry.startsWith(blockConfigPrefix)) {
                    blocks.put(
                            ipToInt(entry.substring(blockConfigPrefix.length())),
                            new BlockInfo(0, 0L, Integer.parseInt(config.getProperty(entry)))
                    );
            }    // if
        } // for
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (httpRequest.getRequestURI() != null && httpRequest.getRequestURI().startsWith(uriAdminPrefix)) {
                // admin actions -> inner configuration
                doAdminAction(httpRequest, httpResponse, this.servletContext);
                return;
            } else  if (IP_HEADERS.size() < globalMaxSize) {
                String ipstr = getClientIpAddr(httpRequest);
                Integer ip = ipToInt(ipstr);
                BlockInfo info = null;
                long now = System.currentTimeMillis();
                if (blocks.containsKey(ip)) {
                    info = blocks.get(ip);
                } else {
                    info = new BlockInfo(0, now, globalMaxRequest);
                    this.blocks.put(ip, info);
                }
                if (now - info.lastReset > intervalMillis) {
                    info.lastReset = now;
                    info.hitCount = 1;
                } else {
                    info.hitCount++;
                    if (info.hitCount > info.maxRequest) {
                        // 429 Too Many Requests
                        String message = "Too Many Request. Max: " + info.maxRequest + " per interval " + intervalMillis / 1000 + "s";
                        servletContext.log("Max Hits IP("+ipstr+"): " + message);
                        httpResponse.sendError(HTTP_TOO_MANY_REQUEST, message);
                        return;
                    }
                } // if test time interval
            } else {
                // if block-storage is "full" delete some old values
                long now = System.currentTimeMillis();
                for (Integer k: blocks.keySet()) {
                    BlockInfo info = blocks.get(k);
                    if (now - info.lastReset > intervalMillis + 1000 // 1" old value
                            && info.maxRequest == globalMaxRequest) // and not explicit configuration
                        blocks.remove(k);
                } // for
            }
            chain.doFilter(request, response);
        } else {
            throw new ServletException("No http servlet?? " + request.getClass().getName());
        }
    }

    /** Self configuration actions */
    private void doAdminAction(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
        Map<String, String> map = new HashMap<>();
        if (httpRequest.getRequestURI().startsWith(uriAdminPrefix+"status")) {
            map = ServletUtils.buildMap(
                            "globalMaxRequest", String.valueOf(globalMaxRequest),
                            "intervalMillis", String.valueOf(intervalMillis),
                            "globalMaxSize", String.valueOf(globalMaxSize));
            for (Integer ip: this.blocks.keySet()) {
                try {
                    String sIP = InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(ip).array()).toString();
                    BlockInfo info = this.blocks.get(ip);
                    map.put("block." + sIP, info.hitCount + ", " + info.lastReset + ", " + info.maxRequest);
                } catch (UnknownHostException e) {
                    servletContext.log("Converting " + ip + ": " + e.getMessage(), e);
                }
            } // for
            ServletUtils.writeJsonToOut(httpResponse, ServletUtils.mapToJsonString(map), servletContext);
        } // if action name pseudo-case
    }

    // methods to get current client IP and transform into 4 byte number

    private Integer ipToInt(String ip) {
        try {
            return fromByteArray( InetAddress.getByName(ip).getAddress() );
        } catch (UnknownHostException e) {
            return -1;
        }
    }

    private int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    protected String getClientIpAddr(HttpServletRequest request) {
        return IP_HEADERS.stream()
                .map(request::getHeader)
                .filter(Objects::nonNull)
                .filter(ip -> !ip.isEmpty() && !ip.equalsIgnoreCase("unknown"))
                .findFirst()
                .orElseGet(request::getRemoteAddr);
    }

    @Override
    public void destroy() {

    }

    // inner class that holds blocking counter
    private class BlockInfo {
        public int hitCount;
        public long lastReset;
        public int maxRequest;

        public BlockInfo(int hit, long last, int max) {
            this.hitCount = hit;
            this.lastReset = last;
            this.maxRequest = max;
        }
    }
}
