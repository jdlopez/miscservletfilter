package es.jdl.auth;

import es.jdl.utils.MockServletContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class TestBasicAuthFilter {

    private BasicAuthenticationFilter filter;
    private HttpServletRequest mockReq;
    private HttpServletResponse mockResp;
    private FilterChain mockFilterChain;
    private FilterConfig mockFilterConfig;

    private String credentialsFile = "/credentials.properties";
    private String goodCredential = null;
    private String fakeCredential = null;

    @Before
    public void init() throws Exception {

        filter = new BasicAuthenticationFilter();
        mockReq = Mockito.mock(HttpServletRequest.class);
        mockResp = Mockito.mock(HttpServletResponse.class);
        mockFilterChain = Mockito.mock(FilterChain.class);
        mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getServletContext()).thenReturn(new MockServletContext());
        Mockito.when(mockFilterConfig.getInitParameter("credentialsFile")).thenReturn(credentialsFile);
        // changed in configuration tests

        String credentials = Files.readAllLines(Paths.get(getClass().getResource(credentialsFile).toURI())).get(0).replace('=',':');
        goodCredential = new String(Base64.getEncoder().encode(credentials.getBytes(StandardCharsets.UTF_8)));
        fakeCredential = new String(Base64.getEncoder().encode((credentials + "fake").getBytes(StandardCharsets.UTF_8)));

    }

    private void testFilterOK(BasicAuthenticationFilter filter) throws ServletException, IOException {
        filter.init(mockFilterConfig);
        filter.doFilter(mockReq, mockResp, mockFilterChain);
        filter.destroy();

        ArgumentCaptor<ServletRequest> acReq = ArgumentCaptor.forClass(ServletRequest.class);
        ArgumentCaptor<ServletResponse> acResp = ArgumentCaptor.forClass(ServletResponse.class);
        Mockito.verify(mockFilterChain, Mockito.atLeastOnce()).doFilter(acReq.capture(), acResp.capture());
    }

    private void testFilterError(BasicAuthenticationFilter filter, String errorMessage) throws ServletException, IOException {
        filter.init(mockFilterConfig);
        filter.doFilter(mockReq, mockResp, mockFilterChain);
        filter.destroy();

        ArgumentCaptor<String> acMsg = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockResp, Mockito.times(1)).sendError(401, errorMessage);
    }

    @Test
    public void testCredentialsOK() throws ServletException, IOException {
        // mock authorization (basic) basicuser:verysimplepassword base64 encoded
        Mockito.when(mockReq.getHeader("Authorization")).thenReturn("Basic " + goodCredential);

        testFilterOK(filter);
    }

    @Test
    public void testNoCredentials() throws ServletException, IOException {
        testFilterError(filter, "Unauthorized");
    }

    @Test
    public void testInvalidToken() throws ServletException, IOException {
        // mock authorization (basic) invalid base64 encoded
        Mockito.when(mockReq.getHeader("Authorization")).thenReturn("Basic " + goodCredential + "not-base64");
        testFilterError(filter, "Invalid authentication token");
    }

    @Test
    public void testInvalidToken2() throws ServletException, IOException {
        Mockito.when(mockReq.getHeader("Authorization")).thenReturn("this-is-not-a-token");
        testFilterError(filter, "Unauthorized");
    }

    @Test
    public void testInvalidCredentials() throws ServletException, IOException {
        // mock authorization (basic) non-existing user-pass
        Mockito.when(mockReq.getHeader("Authorization")).thenReturn("Basic " + fakeCredential);
        testFilterError(filter, "Bad credentials");
    }

    @Test
    public void testConfigurationExternalFile() throws ServletException, IOException {
        // mock config
        Mockito.when(mockFilterConfig.getInitParameter("credentialsFile")).thenReturn("${user.dir}/src/test/resources/credentials.properties");
        // mock authorization (basic) basicuser:verysimplepassword base64 encoded
        Mockito.when(mockReq.getHeader("Authorization")).thenReturn("Basic " + goodCredential);

        testFilterOK(filter);
    }
}
