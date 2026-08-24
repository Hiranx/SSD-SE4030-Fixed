package com.devd.spring.bookstoreapigatewayservice.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * @author: Devaraj Reddy,
 * Date : 2019-05-14 22:12
 */
public class PreFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(PreFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        log.info("PreFilter: " + String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));
        return null;
    }
}
