package com.tuandai.transaction.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.DispatcherType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Configuration
public class DefaultView extends WebMvcConfigurerAdapter{
    @Override
    public void addViewControllers( ViewControllerRegistry registry ) {
        registry.addViewController( "/" ).setViewName( "forward:/index.html" );
        registry.setOrder( Ordered.HIGHEST_PRECEDENCE );
        super.addViewControllers( registry );
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter() {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        JsonConfig mapper = new JsonConfig();
        converter.setObjectMapper(mapper);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.TEXT_HTML);
        mediaTypes.add(MediaType.TEXT_PLAIN);
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        converter.setSupportedMediaTypes(mediaTypes);
        return converter;
    }

    @Bean
    public TokenFilter tokenFilter() {
        return new TokenFilter();
    }

    @Bean
    public FilterRegistrationBean filterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(tokenFilter());//添加过滤器
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.addInitParameter("targetFilterLifecycle", "true");
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        registration.addUrlPatterns("/*");
        registration.addServletNames();
        registration.addInitParameter("static","/static/");
        registration.addInitParameter("exclusions",
                ".js,.gif,.jpg,.png,.css,.ico,/druid/*,.html,/login,/,/agent,/agent/add,/agent/delete,/add/dlq/service,/dlq/serviceQueList,/message/state/count");
        return registration;
        //.eot,.woff,.ttf,
    }


}