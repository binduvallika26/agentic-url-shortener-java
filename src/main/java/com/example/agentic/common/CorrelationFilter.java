package com.example.agentic.common;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationFilter implements Filter {
    public static final String CORRELATION_ID="correlationId";
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var req=(HttpServletRequest)request; var res=(HttpServletResponse)response;
        var supplied=req.getHeader("X-Correlation-ID"); var id=supplied==null||supplied.isBlank()?UUID.randomUUID().toString():supplied;
        req.setAttribute(CORRELATION_ID,id); res.setHeader("X-Correlation-ID",id); MDC.put(CORRELATION_ID,id);
        try { chain.doFilter(request,response); } finally { MDC.remove(CORRELATION_ID); }
    }
    public static String actor(HttpServletRequest request) { var value=request.getHeader("X-Actor"); return value==null||value.isBlank()?"anonymous":value; }
    public static String id(HttpServletRequest request) { return String.valueOf(request.getAttribute(CORRELATION_ID)); }
}
