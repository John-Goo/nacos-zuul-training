/* *****************************************************************************
 * Copyright (C) 2020  QHHQ Co.,Ltd
 * All Rights Reserved.
 * 本软件为物连家美网络技术有限公司研制。未经本公司正式书面同意，其他任何个人、团体
 * 不得使用、复制、修改或发布本软件.
 *****************************************************************************/

package org.crazyit.cloud.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Date;


/**
 * @ClassName: Swagger2Config
 * @Desc: java类作用描述
 * @version: 1.0
 * @author: lingyun
 * @date: 2020/9/18 11:16
 */
@Configuration
@EnableKnife4j
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket demoApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage("org.crazyit.cloud.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    protected ApiInfo getApiInfo(){
        return new ApiInfo("文件管理接口接口文档",
                "文件管理接口接口文档" + new Date(),
                "1.0",
                "",
                new Contact("mfy", "", ""),
                "SpringBoot API接口",
                "",
                new ArrayList<>());
    }

}
