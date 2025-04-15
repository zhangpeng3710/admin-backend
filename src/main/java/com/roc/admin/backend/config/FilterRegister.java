package com.roc.admin.backend.config;


import com.roc.admin.backend.filter.LoginFilter;
import com.roc.admin.backend.filter.UserOperationRecordFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterRegister {
    //    @Bean
    public FilterRegistrationBean<LoginFilter> loginFilter() {
        FilterRegistrationBean<LoginFilter> bean = new FilterRegistrationBean<>();
        //-2147483648 is the highest priority, 2147483647 is the lowest priority
        //2147483647 is the default value if not set order value
        bean.setOrder(1);
        bean.setFilter(new LoginFilter());
        bean.addUrlPatterns("/*");
        return bean;
    }

    //    @Bean
    public FilterRegistrationBean<UserOperationRecordFilter> userRecordFilter() {
        FilterRegistrationBean<UserOperationRecordFilter> bean = new FilterRegistrationBean<>();
        bean.setOrder(2);
        bean.setFilter(new UserOperationRecordFilter());
        bean.addUrlPatterns("/*");
        return bean;
    }

}
