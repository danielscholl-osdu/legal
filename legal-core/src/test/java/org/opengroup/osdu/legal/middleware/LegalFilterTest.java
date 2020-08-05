package org.opengroup.osdu.legal.middleware;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.Request;
import org.opengroup.osdu.core.common.model.http.RequestInfo;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class LegalFilterTest {

    @Mock
    private DpsHeaders headers;

    @Mock
    private RequestInfo requestInfo;

    @Mock
    private JaxRsDpsLog logger;

    @InjectMocks
    private LegalFilter legalFilter;

    @Test
    public void shouldSetCorrectResponseHeaders() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Mockito.when(requestInfo.getUri()).thenReturn("https://test.com");
        Mockito.when(requestInfo.isHttps()).thenReturn(true);
        Mockito.when(headers.getAuthorization()).thenReturn("authorization-header-value");
        Mockito.when(headers.getCorrelationId()).thenReturn("correlation-id-value");
        Mockito.when(httpServletRequest.getMethod()).thenReturn("POST");

        legalFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Origin", Collections.singletonList("*").toString());
        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Headers", Collections.singletonList("origin, content-type, accept, authorization, data-partition-id, correlation-id, appkey").toString());
        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Methods", Collections.singletonList("GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH").toString());
        Mockito.verify(httpServletResponse).addHeader("Access-Control-Allow-Credentials", Collections.singletonList("true").toString());
        Mockito.verify(httpServletResponse).addHeader("X-Frame-Options", Collections.singletonList("DENY").toString());
        Mockito.verify(httpServletResponse).addHeader("X-XSS-Protection", Collections.singletonList("1; mode=block").toString());
        Mockito.verify(httpServletResponse).addHeader("X-Content-Type-Options", Collections.singletonList("nosniff").toString());
        Mockito.verify(httpServletResponse).addHeader("Cache-Control", Collections.singletonList("no-cache, no-store, must-revalidate").toString());
        Mockito.verify(httpServletResponse).addHeader("Content-Security-Policy", Collections.singletonList("default-src 'self'").toString());
        Mockito.verify(httpServletResponse).addHeader("Strict-Transport-Security", Collections.singletonList("max-age=31536000; includeSubDomains").toString());
        Mockito.verify(httpServletResponse).addHeader("Expires", Collections.singletonList("0").toString());
        Mockito.verify(httpServletResponse).addHeader("correlation-id", "correlation-id-value");
        Mockito.verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        Mockito.verify(logger).request(Mockito.any(Request.class));
    }
}
