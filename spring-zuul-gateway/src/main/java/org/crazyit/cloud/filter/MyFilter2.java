//package org.crazyit.cloud.filter;
///*==========================================================================
// * Copyright (C) Wit2Cloud Co.,Ltd
// * All Rights Reserved.
// * Created By 开源学社
// ==========================================================================*/
//
//import com.alibaba.fastjson.JSON;
//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.context.RequestContext;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
//import org.springframework.cloud.netflix.zuul.filters.Route;
//import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
//import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
//import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
//import org.springframework.web.util.UrlPathHelper;
//
//import javax.annotation.Resource;
//import java.net.MalformedURLException;
//import java.net.URL;
//
///**
// * @author John Goo
// * @version 1.0
// * @ClassName: MyFilter
// * @Desc: TODO
// * @history v1.0
// */
//@Slf4j
//public class MyFilter2 extends ZuulFilter {
//
//
//    /**
//     * @deprecated use {@link FilterConstants#PRE_DECORATION_FILTER_ORDER}
//     */
//    @Deprecated
//    public static final int FILTER_ORDER = 1;
//
//
//    @Resource
//    private RouteLocator routeLocator;
//
//    private String dispatcherServletPath;
//
//    private ZuulProperties properties;
//
//    private UrlPathHelper urlPathHelper = new UrlPathHelper();
//
//    private ProxyRequestHelper proxyRequestHelper;
//
//    public MyFilter2() {
//    }
//
//    public MyFilter2(RouteLocator routeLocator, String dispatcherServletPath,
//                     ZuulProperties properties, ProxyRequestHelper proxyRequestHelper) {
//        this.routeLocator = routeLocator;
//        this.properties = properties;
//        this.urlPathHelper.setRemoveSemicolonContent(properties.isRemoveSemicolonContent());
//        this.urlPathHelper.setUrlDecode(properties.isDecodeUrl());
//        this.dispatcherServletPath = dispatcherServletPath;
//        this.proxyRequestHelper = proxyRequestHelper;
//    }
//
//    @Override
//    public int filterOrder() {
//        return 1;
//    }
//
//    @Override
//    public String filterType() {
//        return "pre";
//    }
//
//    /**
//     * 判断是否适应当前请求
//     * 1. 若网关自身不提供对外业务接口，相当于1个请求只在网关与外部系统路由1次，则FORWARD_TO_KEY、SERVICE_ID_KEY为空则直接适用
//     * 2. 若网关自身对外提供服务，最终会路由到自身，相当于网关第1次网关 --> 网关业务接口，网关充当双重角色，这种场景第2次访问网关相当于只访问业务接口，过滤器不生效
//     */
//    @Override
//    public boolean shouldFilter() {
//        return true;
//    }
//
//    @Override
//    public Object run() {
//        RequestContext ctx = RequestContext.getCurrentContext();
//        final String requestURI = this.urlPathHelper.getPathWithinApplication(ctx.getRequest());
//        // 获取请求路由信息
//        Route route = this.routeLocator.getMatchingRoute(requestURI);
//
//
//        System.out.println(" >>> Route:" + JSON.toJSON(route));
//        String location = "http://localhost:7001";
//        log.info(" == location："+location);
//        ctx.setRouteHost(getUrl(location));
//        ctx.addOriginResponseHeader(FilterConstants.X_FORWARDED_HOST_HEADER, "127.0.0.1");
//        ctx.addOriginResponseHeader(FilterConstants.X_FORWARDED_PORT_HEADER, "7001");
//       // String originalRequestPath = ctx.get(FilterConstants.REQUEST_URI_KEY).toString();
//        //log.info(" orginPath:"+originalRequestPath);
//        ctx.put(FilterConstants.REQUEST_URI_KEY,route.getPath());
//        this.routeLocator.getMatchingRoute(requestURI).setLocation(location);
//        System.out.println(" >>>修改后 Route:" + JSON.toJSON(this.routeLocator.getMatchingRoute(requestURI)));
//        return null;
//    }
//
//    private URL getUrl(String target) {
//        try {
//            return new URL(target);
//        } catch (MalformedURLException ex) {
//            throw new IllegalStateException("Target URL is malformed", ex);
//        }
//    }
//}
