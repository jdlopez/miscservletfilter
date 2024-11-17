package es.jdl.response;

import es.jdl.utils.MockHeadersHttpResponse;
import es.jdl.utils.MockServletContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AddHeadersFilterTest {
    private AddHeadersFilter filter;
    private HttpServletRequest mockReq;
    private HttpServletResponse mockResp;
    private FilterChain mockFilterChain;
    private FilterConfig mockFilterConfig;

    private String headerName = null;
    private String headerValue = null;

    @Before
    public void init() throws Exception {

        filter = new AddHeadersFilter();
        mockReq = Mockito.mock(HttpServletRequest.class);
        mockResp = new MockHeadersHttpResponse(); // TODO change for mock methos: Mockito.mock and ArgumentCaptor
        mockFilterChain = Mockito.mock(FilterChain.class);
        mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getServletContext()).thenReturn(new MockServletContext());
        // set default config

        String headerLine = Files.readAllLines(Paths.get(getClass().getResource("/AddHeadersFilter.properties").toURI())).get(0);
        String[] headers = headerLine.split("=");
        headerName = headers[0].substring("AddHeadersFilter.header.".length());
        headerValue = headers[1];
    }

    @Test
    public void testFilterOK() throws ServletException, IOException {
        filter.init(mockFilterConfig);
        filter.doFilter(mockReq, mockResp, mockFilterChain);
        filter.destroy();

        Assert.assertEquals(headerValue, mockResp.getHeader(headerName));
    }

}
