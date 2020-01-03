package es.jdl.web;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestFilter {

    @Test
    public void testDoFilter() throws ServletException, IOException {

        BasicAuthenticationFilter filter = new BasicAuthenticationFilter();

        HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockResp = Mockito.mock(HttpServletResponse.class);
        FilterChain mockFilterChain = Mockito.mock(FilterChain.class);

        FilterConfig mockFilterConfig = Mockito.mock(FilterConfig.class);
        // mock authorization (basic) basicuser:verysimplepassword base64 encoded
        Mockito.when(mockReq.getHeader("Authorization")).thenReturn("Basic Ym FzaWN1c2VyOnZlcnlzaW1wbGVwYXNzd29yZA==");
        // mock config
        Mockito.when(mockFilterConfig.getInitParameter("credentialsFile")).thenReturn("/credentials.properties");
        Mockito.when(mockFilterConfig.getServletContext()).thenReturn(new MockServletContext());

        filter.init(mockFilterConfig);
        filter.doFilter(mockReq, mockResp, mockFilterChain);
        filter.destroy();

        ArgumentCaptor<ServletRequest> acReq = ArgumentCaptor.forClass(ServletRequest.class);
        ArgumentCaptor<ServletResponse> acResp = ArgumentCaptor.forClass(ServletResponse.class);
        //Mockito.verify(mockResp, Mockito.times(1)).sendError(401, "Unauthorized");
        Mockito.verify(mockFilterChain, Mockito.atLeastOnce()).doFilter(acReq.capture(), acResp.capture());
    }
}
