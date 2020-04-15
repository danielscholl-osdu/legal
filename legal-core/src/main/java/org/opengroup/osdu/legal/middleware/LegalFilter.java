package org.opengroup.osdu.legal.middleware;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Inject;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.opengroup.osdu.core.common.http.ResponseHeaders;
import org.opengroup.osdu.core.common.model.http.Request;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.opengroup.osdu.core.common.model.http.RequestInfo;

@Component
@Lazy
public class LegalFilter implements Filter {

    @Inject
    private IAuthorizationService authorizationServiceEntitlements;

    @Inject
    private DpsHeaders headers;

    @Inject
    private RequestInfo requestInfo;

    @Inject
    private JaxRsDpsLog logger;

    @Value("${ACCEPT_HTTP:false}")
    private boolean acceptHttp;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        headers.addCorrelationIdIfMissing();
        long startTime = System.currentTimeMillis();
        setResponseHeaders(httpServletResponse);
        try {
            if (!validateIsHttps(httpServletResponse)) {
                //do nothing
            } else if (httpServletRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
                httpServletResponse.setStatus(200);
            } else {
                chain.doFilter(request, response);
            }
        }finally {
            logRequest(httpServletRequest, httpServletResponse, startTime);
        }
        
	}

	@Override
	public void destroy() {
    }
    
    private boolean validateIsHttps( HttpServletResponse httpServletResponse) {
        String uri = requestInfo.getUri();
        if(!isLocalHost(uri) && !isCronJob(uri) && !isSwagger(uri)) {
            if(!hasJwt()) {
                httpServletResponse.setStatus(401);
                return false;
            }
            else if(!isAcceptHttp() && !requestInfo.isHttps()) {
                String location = uri.replaceFirst("http", "https");
                httpServletResponse.setStatus(307);
                httpServletResponse.addHeader("location", location);
                return false;
            }
        }
        return true;
    }

    private boolean hasJwt() {
        String authorization = headers.getAuthorization();
        return (authorization != null) && (authorization.length() > 0);
    }

    private boolean isLocalHost(String uri) {
        return (uri.contains("//localhost") || uri.contains("//127.0.0.1"));
    }

    private boolean isCronJob(String uri) {
        return uri.contains("/jobs/updateLegalTagStatus");
    }
    private boolean isSwagger(String uri) {
        return uri.contains("/swagger") || uri.contains("/v2/api-docs") || uri.contains("/configuration/ui") || uri.contains("/webjars/");
    }
    private void logRequest(HttpServletRequest servletRequest, HttpServletResponse servletResponse, long startTime) {
        String uri = requestInfo.getUri();
        if(!uri.endsWith("/liveness_check") && !uri.endsWith("/readiness_check")) {
            logger.request(Request.builder()
                    .requestMethod(servletRequest.getMethod())
                    .latency(Duration.ofMillis(System.currentTimeMillis() - startTime))
                    .requestUrl(uri)
                    .Status(servletResponse.getStatus())
                    .ip(servletRequest.getRemoteAddr())
                    .build());
        }
    }

    private void setResponseHeaders(HttpServletResponse httpServletResponse) {
        Map<String, List<Object>> standardHeaders = ResponseHeaders.STANDARD_RESPONSE_HEADERS;
        for (Map.Entry<String, List<Object>> header : standardHeaders.entrySet()) {
            httpServletResponse.addHeader(header.getKey(), header.getValue().toString());
        }
        httpServletResponse.addHeader(DpsHeaders.CORRELATION_ID, this.headers.getCorrelationId());
    }

    public boolean isAcceptHttp() {
        return acceptHttp;
    }
}