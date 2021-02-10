package org.crazyit.cloud.filter;
/*==========================================================================
 * Copyright (C) Wit2Cloud Co.,Ltd
 * All Rights Reserved.
 * Created By 开源学社
 ==========================================================================*/

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.crazyit.cloud.common.WResult;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UrlPathHelper;

import javax.annotation.Resource;
import javax.jws.WebResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author John Goo
 * @version 1.0
 * @ClassName: MyFilter
 * @Desc: TODO
 * @history v1.0
 */
@Slf4j
public class MyFilter extends ZuulFilter {


    /**
     * @deprecated use {@link FilterConstants#PRE_DECORATION_FILTER_ORDER}
     */
    @Deprecated
    public static final int FILTER_ORDER = 1;


    @Resource
    private RouteLocator routeLocator;


    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    private ProxyRequestHelper proxyRequestHelper;

    public MyFilter() {
    }


    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public String filterType() {
        return  FilterConstants.ROUTE_TYPE;
    }

    /**
     * 判断是否适应当前请求
     * 1. 若网关自身不提供对外业务接口，相当于1个请求只在网关与外部系统路由1次，则FORWARD_TO_KEY、SERVICE_ID_KEY为空则直接适用
     * 2. 若网关自身对外提供服务，最终会路由到自身，相当于网关第1次网关 --> 网关业务接口，网关充当双重角色，这种场景第2次访问网关相当于只访问业务接口，过滤器不生效
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        HttpServletResponse response = ctx.getResponse();
        String memberIdStr = request.getHeader("memberId");
        String token = request.getHeader("token");
        long memberId =0L;
        if(memberIdStr != null ){
            memberId = Long.valueOf(memberIdStr);
        }
        if(StringUtils.isBlank(token)) {
            WResult result = WResult.newInstance();
            //如果token 等于null   返回状态码和没有权限的信息给前台
            response.setHeader("Content-type", "application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            result.failer(401,"令牌不正确！");
            ctx.setResponseBody(JSON.toJSON(result).toString());
            return null;
        }

        // 灰度环境
        if(memberId < 100) {
            final String requestURI = this.urlPathHelper.getPathWithinApplication(ctx.getRequest());
            log.info("  requestURI:"+requestURI);
            log.info("  http uri:"+request.getRequestURI());
            // 获取请求路由信息
            Route route = this.routeLocator.getMatchingRoute(requestURI);
            System.out.println(" >>> Route:" + JSON.toJSON(route));
            String location = "http://localhost:7001";
            log.info(" == location：" + location);
            ctx.setRouteHost(getUrl(location));
        }
        return null;
    }

    private URL getUrl(String target) {
        try {
            return new URL(target);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Target URL is malformed", ex);
        }
    }
}
