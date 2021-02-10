//package org.crazyit.cloud.filter;
///*==========================================================================
// * Copyright (C) Wit2Cloud Co.,Ltd
// * All Rights Reserved.
// * Created By 开源学社
// ==========================================================================*/
//
//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.context.RequestContext;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
//import org.springframework.cloud.netflix.zuul.filters.Route;
//import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
//import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
//import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
//import org.springframework.cloud.netflix.zuul.util.RequestUtils;
//import org.springframework.util.StringUtils;
//import org.springframework.web.util.UrlPathHelper;
//
//import javax.servlet.http.HttpServletRequest;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.regex.Pattern;
//
///**
// * @author John Goo
// * @version 1.0
// * @ClassName: MyFilter
// * @Desc: TODO
// * @history v1.0
// */
//@Slf4j
//public class MyFilter_bak extends ZuulFilter {
//
//
//    /**
//     * @deprecated use {@link FilterConstants#PRE_DECORATION_FILTER_ORDER}
//     */
//    @Deprecated
//    public static final int FILTER_ORDER = 1;
//
//    /**
//     * A double slash pattern.
//     */
//    public static final Pattern DOUBLE_SLASH = Pattern.compile("//");
//    private static final String REQUEST_URI_KEY = ;
//
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
//    public MyFilter_bak(RouteLocator routeLocator, String dispatcherServletPath,
//                        ZuulProperties properties, ProxyRequestHelper proxyRequestHelper) {
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
//
//        // 获取请求路由信息
//        Route route = this.routeLocator.getMatchingRoute(requestURI);
//        if (route != null) {
//            String location = route.getLocation();
//            if (location != null) {
//                ctx.put(REQUEST_URI_KEY, route.getPath());
//                ctx.put(PROXY_KEY, route.getId());
//
//                // 自定义敏感头处理：如果开启则RequestContext添加属性，对应请求头信息会被忽略：ignoredHeaders，默认："Cookie", "Set-Cookie", "Authorization"
//                // 注意：很多系统需要使用Authorization鉴权，就需要自定义且重写敏感头信息，否则目标服务拿不到Authorization，导致鉴权失败
//                if (!route.isCustomSensitiveHeaders()) {
//                    this.proxyRequestHelper.addIgnoredHeaders(
//                            this.properties.getSensitiveHeaders().toArray(new String[0]));
//                }
//                else {
//                    this.proxyRequestHelper.addIgnoredHeaders(
//                            route.getSensitiveHeaders().toArray(new String[0]));
//                }
//
//                // 异常重试设置
//                if (route.getRetryable() != null) {
//                    ctx.put("retryable", route.getRetryable());
//                }
//
//                // location 以http或https开发头，则对应SimpleHostRoutingFilter
//                if (location.startsWith("http" + ":")
//                        || location.startsWith("https" + ":")) {
//                    ctx.setRouteHost(getUrl(location));
//                    ctx.addOriginResponseHeader(SERVICE_HEADER, location);
//                }
//                // location 以forward:开头，则对应SendForwardFilter
//                else if (location.startsWith(FORWARD_LOCATION_PREFIX)) {
//                    ctx.set(FORWARD_TO_KEY,
//                            StringUtils.cleanPath(
//                                    location.substring(FORWARD_LOCATION_PREFIX.length())
//                                            + route.getPath()));
//                    ctx.setRouteHost(null);
//                    return null;
//                }
//                // 其他：则是服务名访问，若以服务名称访问，则location默认serviceID,详细参考：DiscoveryClientRouteLocator.locateRoutes
//                else {
//                    // set serviceId for use in filters.route.RibbonRequest
//                    ctx.set("serviceId", location);
//                    ctx.setRouteHost(null);
//                    ctx.addOriginResponseHeader(SERVICE_ID_HEADER, location);
//                }
//                // Http代理相关处理
//                if (this.properties.isAddProxyHeaders()) {
//                    addProxyHeaders(ctx, route);
//                    String xforwardedfor = ctx.getRequest()
//                            .getHeader(X_FORWARDED_FOR_HEADER);
//                    String remoteAddr = ctx.getRequest().getRemoteAddr();
//                    if (xforwardedfor == null) {
//                        xforwardedfor = remoteAddr;
//                    }
//                    else if (!xforwardedfor.contains(remoteAddr)) { // Prevent duplicates
//                        xforwardedfor += ", " + remoteAddr;
//                    }
//                    ctx.addZuulRequestHeader(X_FORWARDED_FOR_HEADER, xforwardedfor);
//                }
//                if (this.properties.isAddHostHeader()) {
//                    ctx.addZuulRequestHeader(HttpHeaders.HOST,
//                            toHostHeader(ctx.getRequest()));
//                }
//            }
//        }
//        // 默认请求转发
//        else {
//            log.warn("No route found for uri: " + requestURI);
//            String forwardURI = getForwardUri(requestURI);
//
//            ctx.set(FORWARD_TO_KEY, forwardURI);
//        }
//        return null;
//    }
//
//    /* for testing */
//    String getForwardUri(String requestURI) {
//        // default fallback servlet is DispatcherServlet
//        String fallbackPrefix = this.dispatcherServletPath;
//
//        String fallBackUri = requestURI;
//        if (RequestUtils.isZuulServletRequest()) {
//            // remove the Zuul servletPath from the requestUri
//            log.debug("zuulServletPath=" + this.properties.getServletPath());
//            fallBackUri = fallBackUri.replaceFirst(this.properties.getServletPath(), "");
//            log.debug("Replaced Zuul servlet path:" + fallBackUri);
//        }
//        else if (this.dispatcherServletPath != null) {
//            // remove the DispatcherServlet servletPath from the requestUri
//            log.debug("dispatcherServletPath=" + this.dispatcherServletPath);
//            fallBackUri = fallBackUri.replaceFirst(this.dispatcherServletPath, "");
//            log.debug("Replaced DispatcherServlet servlet path:" + fallBackUri);
//        }
//        if (!fallBackUri.startsWith("/")) {
//            fallBackUri = "/" + fallBackUri;
//        }
//
//        String forwardURI = (fallbackPrefix == null) ? fallBackUri
//                : fallbackPrefix + fallBackUri;
//        forwardURI = DOUBLE_SLASH.matcher(forwardURI).replaceAll("/");
//        return forwardURI;
//    }
//
//    private void addProxyHeaders(RequestContext ctx, Route route) {
//        HttpServletRequest request = ctx.getRequest();
//        String host = toHostHeader(request);
//        String port = String.valueOf(request.getServerPort());
//        String proto = request.getScheme();
//        if (hasHeader(request, X_FORWARDED_HOST_HEADER)) {
//            host = request.getHeader(X_FORWARDED_HOST_HEADER) + "," + host;
//        }
//        if (!hasHeader(request, X_FORWARDED_PORT_HEADER)) {
//            if (hasHeader(request, X_FORWARDED_PROTO_HEADER)) {
//                StringBuilder builder = new StringBuilder();
//                for (String previous : StringUtils.commaDelimitedListToStringArray(
//                        request.getHeader(X_FORWARDED_PROTO_HEADER))) {
//                    if (builder.length() > 0) {
//                        builder.append(",");
//                    }
//                    builder.append(
//                            HTTPS_SCHEME.equals(previous) ? HTTPS_PORT : HTTP_PORT);
//                }
//                builder.append(",").append(port);
//                port = builder.toString();
//            }
//        }
//        else {
//            port = request.getHeader(X_FORWARDED_PORT_HEADER) + "," + port;
//        }
//        if (hasHeader(request, X_FORWARDED_PROTO_HEADER)) {
//            proto = request.getHeader(X_FORWARDED_PROTO_HEADER) + "," + proto;
//        }
//        ctx.addZuulRequestHeader(X_FORWARDED_HOST_HEADER, host);
//        ctx.addZuulRequestHeader(X_FORWARDED_PORT_HEADER, port);
//        ctx.addZuulRequestHeader(X_FORWARDED_PROTO_HEADER, proto);
//        addProxyPrefix(ctx, route);
//    }
//
//    private boolean hasHeader(HttpServletRequest request, String name) {
//        return StringUtils.hasLength(request.getHeader(name));
//    }
//
//    private void addProxyPrefix(RequestContext ctx, Route route) {
//        String forwardedPrefix = ctx.getRequest().getHeader(X_FORWARDED_PREFIX_HEADER);
//        String contextPath = ctx.getRequest().getContextPath();
//        String prefix = StringUtils.hasLength(forwardedPrefix) ? forwardedPrefix
//                : (StringUtils.hasLength(contextPath) ? contextPath : null);
//        if (StringUtils.hasText(route.getPrefix())) {
//            StringBuilder newPrefixBuilder = new StringBuilder();
//            if (prefix != null) {
//                if (prefix.endsWith("/") && route.getPrefix().startsWith("/")) {
//                    newPrefixBuilder.append(prefix, 0, prefix.length() - 1);
//                }
//                else {
//                    newPrefixBuilder.append(prefix);
//                }
//            }
//            newPrefixBuilder.append(route.getPrefix());
//            prefix = newPrefixBuilder.toString();
//        }
//        if (prefix != null) {
//            ctx.addZuulRequestHeader(X_FORWARDED_PREFIX_HEADER, prefix);
//        }
//    }
//
//    private String toHostHeader(HttpServletRequest request) {
//        int port = request.getServerPort();
//        if ((port == HTTP_PORT && HTTP_SCHEME.equals(request.getScheme()))
//                || (port == HTTPS_PORT && HTTPS_SCHEME.equals(request.getScheme()))) {
//            return request.getServerName();
//        }
//        else {
//            return request.getServerName() + ":" + port;
//        }
//    }
//
//    private URL getUrl(String target) {
//        try {
//            return new URL(target);
//        }
//        catch (MalformedURLException ex) {
//            throw new IllegalStateException("Target URL is malformed", ex);
//        }
//    }