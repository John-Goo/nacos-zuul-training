package org.crazyit.cloud.filter;
/*==========================================================================
 * Copyright (C) Wit2Cloud Co.,Ltd
 * All Rights Reserved.
 * Created By 开源学社
 ==========================================================================*/

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.crazyit.cloud.common.WResult;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;

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

    // 灰度服务组
    public static final String _GRAY_GROUP = "gray";


    @Resource
    private RouteLocator routeLocator;

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

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
        String version = request.getHeader("version");
        String token = request.getHeader("token");

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
        if("v1.5.0".compareTo(version) < 0) {
            final String requestURI = this.urlPathHelper.getPathWithinApplication(ctx.getRequest());
            log.info("  requestURI:"+requestURI);
            // 获取请求路由信息
            Route route = this.routeLocator.getMatchingRoute(requestURI);
            String serviceId = route.getLocation();
            //获取服务发现客户端
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
            try {
                Instance instance =  namingService.selectOneHealthyInstance(serviceId,_GRAY_GROUP);
                log.info(" >>> Route:" + JSON.toJSON(route));
                if(instance == null){
                    // 非灰度服务
                    return null;
                }
                String location = String.format("http://%s:%s",instance.getIp(),instance.getPort());
                log.info(" == 转发灰度服务location：" + location);
                ctx.setRouteHost(getUrl(location));
            } catch (NacosException e) {
                log.error(" 获取动态IP发生了异常",e);
            }
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
