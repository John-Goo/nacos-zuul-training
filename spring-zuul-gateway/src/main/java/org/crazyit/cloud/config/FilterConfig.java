package org.crazyit.cloud.config;
/*==========================================================================
 * Copyright (C) Wit2Cloud Co.,Ltd
 * All Rights Reserved.
 * Created By 开源学社
 ==========================================================================*/

import org.crazyit.cloud.filter.MyFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author John Goo
 * @version 1.0
 * @ClassName: FilterConfig
 * @Desc: TODO
 * @history v1.0
 */
@Component
public class FilterConfig {

    @Bean
    public MyFilter myFilter(){
        return new MyFilter();
    }

}
