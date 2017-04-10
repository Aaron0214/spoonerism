package com.aaron.spoonerism.config;

import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.context.ContextLifecycleFilter;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.groovy.GroovyFileFilter;
import com.netflix.zuul.http.ZuulServlet;
import com.netflix.zuul.monitoring.MonitoringHelper;
import javax.annotation.PostConstruct;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZuulConfig {

    /** It performs the core Zuul Filter flow of executing pre, routing, and post Filters. */
    @Bean
    public ServletRegistrationBean zuulServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new ZuulServlet());
        registration.addUrlMappings("/*");
        return registration;
    }

    /** It cleans up the RequestContext after each request, ensuring isolation. */
    @Bean
    public FilterRegistrationBean contextLifecycleFilter() {
        FilterRegistrationBean filter = new FilterRegistrationBean(new ContextLifecycleFilter());
        filter.addUrlPatterns("/*");
        return filter;
    }

    @PostConstruct
    public void startServer() {
        // mocks monitoring infrastructure as we don't need it for this simple app
        MonitoringHelper.initMocks();
        // initializes groovy filesystem poller
        initGroovyFilterManager();
    }

    private void initGroovyFilterManager() {
        FilterLoader.getInstance().setCompiler(new GroovyCompiler());

        String scriptRoot = "src/main/groovy/filters/";
        try {
            FilterFileManager.setFilenameFilter(new GroovyFileFilter());
            FilterFileManager.init(5, scriptRoot + "pre", scriptRoot + "route", scriptRoot + "post");//每隔5s去拉取一次
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
