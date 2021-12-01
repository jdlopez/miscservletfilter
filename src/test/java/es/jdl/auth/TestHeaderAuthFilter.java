package es.jdl.auth;

import es.jdl.utils.MockServletContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestHeaderAuthFilter {
    private HeaderAuthenticationFilter filter;
    private HttpServletRequest mockReq;
    private HttpServletResponse mockResp;
    private FilterChain mockFilterChain;
    private FilterConfig mockFilterConfig;

    private String goodCredential = null;
    private String fakeCredential = null;

    @Before
    public void init() throws Exception {

        filter = new HeaderAuthenticationFilter();
        mockReq = Mockito.mock(HttpServletRequest.class);
        mockResp = Mockito.mock(HttpServletResponse.class);
        mockFilterChain = Mockito.mock(FilterChain.class);
        mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getServletContext()).thenReturn(new MockServletContext());
        // set default config

        String credentials = Files.readAllLines(Paths.get(getClass().getResource("/HeaderAuthenticationFilter.properties").toURI())).get(0);
        goodCredential = credentials.substring("HeaderAuthenticationFilter.key.".length(), credentials.indexOf('='));
        fakeCredential = "fake";

    }

    private void testFilterOK(HeaderAuthenticationFilter filter) throws ServletException, IOException {
        filter.init(mockFilterConfig);
        filter.doFilter(mockReq, mockResp, mockFilterChain);
        filter.destroy();

        ArgumentCaptor<ServletRequest> acReq = ArgumentCaptor.forClass(ServletRequest.class);
        ArgumentCaptor<ServletResponse> acResp = ArgumentCaptor.forClass(ServletResponse.class);
        Mockito.verify(mockFilterChain, Mockito.atLeastOnce()).doFilter(acReq.capture(), acResp.capture());
    }

    private void testFilterError(HeaderAuthenticationFilter filter, String errorMessage) throws ServletException, IOException {
        filter.init(mockFilterConfig);
        filter.doFilter(mockReq, mockResp, mockFilterChain);
        filter.destroy();

        ArgumentCaptor<String> acMsg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockResp, Mockito.times(1)).sendError(401, errorMessage);
    }

    @Test
    public void testCredentialsOK() throws ServletException, IOException {
        Mockito.when(mockReq.getHeader(HeaderAuthenticationFilter.API_KEY_HEADER)).thenReturn(goodCredential);

        testFilterOK(filter);
    }

    @Test
    public void testNoCredentials() throws ServletException, IOException {
        testFilterError(filter, "Unauthorized");
    }

    @Test
    public void testInvalidToken() throws ServletException, IOException {
        Mockito.when(mockReq.getHeader(HeaderAuthenticationFilter.API_KEY_HEADER)).thenReturn(fakeCredential);
        testFilterError(filter, "Unauthorized");
    }

    @Test
    public void testConfigurationExternalFile() throws ServletException, IOException {
        // mock config
        Mockito.when(mockFilterConfig.getInitParameter("configFile")).thenReturn("${user.dir}/src/test/resources/HeaderAuthenticationFilter.properties");
        // mock authorization (basic) basicuser:verysimplepassword base64 encoded
        Mockito.when(mockReq.getHeader(HeaderAuthenticationFilter.API_KEY_HEADER)).thenReturn(goodCredential);

        testFilterOK(filter);
    }
}

