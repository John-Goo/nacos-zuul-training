package org.crazyit.cloud.controller;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.crazyit.cloud.common.WResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Api(value = "Nacos服务API",tags = "Nacos服务API")
@RestController
public class NacosAPIDemoController {

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosAddr;

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;


    @ApiOperation(value = "查询" )
    @RequestMapping(value = "/nacos/routeList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WResult routeList(@RequestParam("serviceId") String serviceId) {
        WResult wResult = WResult.newInstance();
        System.out.println("接收到参数===>"+serviceId);
        try {
            //获取服务发现客户端
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();

            // 获取指定的服务实例列表
            List<Instance> allInstances = namingService.getAllInstances(serviceId,"gray");
            log.info("[ribbon负载均衡策略] 可用的服务实例: {}", allInstances);
            for(Instance instance :allInstances){
                log.info("=== instance:"+ JSON.toJSON(instance));
            }
            NamingService naming = NamingFactory.createNamingService(nacosAddr);
            System.out.println(naming.selectInstances(serviceId, "gray",true));
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return wResult;


}

public String nacosLogin() {
        String msg = null;

    try {
        // Initialize the configuration service, and the console automatically obtains the following parameters through the sample code.
        Properties properties = new Properties();
        properties.put("serverAddr", nacosAddr);

        // if need username and password to login
        properties.put("username", "nacos");
        properties.put("password", "nacos");

        ConfigService configService = NacosFactory.createConfigService(properties);
        msg  = configService.getServerStatus();
        System.out.printf("status:"+configService.getServerStatus());


    } catch (NacosException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return msg;
}

}