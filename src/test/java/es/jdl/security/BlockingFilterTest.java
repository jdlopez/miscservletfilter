package es.jdl.security;

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

public class BlockingFilterTest {

    // WATCH values in test properties!!
    private static final String EXPECTED_ERROR_MESSAGE = "Too Many Request. Max: 1 per interval 1s";

    private BlockingFilter filter;
    private HttpServletRequest mockReq;
    private HttpServletResponse mockResp;
    private FilterChain mockFilterChain;
    private FilterConfig mockFilterConfig;

    @Before
    public void init() throws Exception {

        filter = new BlockingFilter();
        mockReq = Mockito.mock(HttpServletRequest.class);
        mockResp = Mockito.mock(HttpServletResponse.class);
        mockFilterChain = Mockito.mock(FilterChain.class);
        mockFilterConfig = Mockito.mock(FilterConfig.class);
        Mockito.when(mockFilterConfig.getServletContext()).thenReturn(new MockServletContext());
        // set default config in BlockingFilter,properties

    }

    @Test
    public void testPass() throws ServletException, IOException, InterruptedException {
        Mockito.when(mockReq.getRemoteAddr()).thenReturn("1.2.3.4");

        filter.init(mockFilterConfig);
        // 1st
        filter.doFilter(mockReq, mockResp, mockFilterChain);
        // 2nd
        filter.doFilter(mockReq, mockResp, mockFilterChain);

        Thread.sleep(1000); // reset counters?
        // 1st - 3rd
        filter.doFilter(mockReq, mockResp, mockFilterChain);
        // 2nd - 4th
        filter.doFilter(mockReq, mockResp, mockFilterChain);

        filter.destroy();

        ArgumentCaptor<ServletRequest> acReq = ArgumentCaptor.forClass(ServletRequest.class);
        ArgumentCaptor<ServletResponse> acResp = ArgumentCaptor.forClass(ServletResponse.class);
        // will pass 1st and 3dr time
        Mockito.verify(mockFilterChain, Mockito.times(2)).doFilter(acReq.capture(), acResp.capture());
        // then send error at 2nd and 4th
        Mockito.verify(mockResp, Mockito.times(2)).sendError(BlockingFilter.HTTP_TOO_MANY_REQUEST,
                EXPECTED_ERROR_MESSAGE); // WATCH values in test properties!!
    }

    @Test
    public void test2Clients() throws ServletException, IOException, InterruptedException {
        HttpServletRequest mockReq2 = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockReq.getRemoteAddr()).thenReturn("1.2.3.4");
        Mockito.when(mockReq2.getRemoteAddr()).thenReturn("5.6.7.8");

        filter.init(mockFilterConfig);
        filter.doFilter(mockReq2, mockResp, mockFilterChain);
        for (int i = 0; i < 2; i++)
            filter.doFilter(mockReq, mockResp, mockFilterChain);

        Thread.sleep(1000); // reset counters?
        for (int i = 0; i < 2; i++)
            filter.doFilter(mockReq2, mockResp, mockFilterChain);
        for (int i = 0; i < 2; i++)
            filter.doFilter(mockReq, mockResp, mockFilterChain);

        filter.destroy();

        ArgumentCaptor<ServletRequest> acReq = ArgumentCaptor.forClass(ServletRequest.class);
        ArgumentCaptor<ServletResponse> acResp = ArgumentCaptor.forClass(ServletResponse.class);
        // will pass 1st and 3dr time
        Mockito.verify(mockFilterChain, Mockito.times(4)).doFilter(acReq.capture(), acResp.capture());
        // then send error at 2nd and 4th
        Mockito.verify(mockResp, Mockito.times(3)).sendError(BlockingFilter.HTTP_TOO_MANY_REQUEST,
                EXPECTED_ERROR_MESSAGE);
    }

}