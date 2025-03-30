package com.mygo.config;

import com.mygo.interceptor.LoginInterceptor;
import com.mygo.interceptor.RefreshInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    private final RefreshInterceptor refreshInterceptor;

    @Value("${mygo.static.img}")
    private String staticResourcePath;

    @Autowired
    public MvcConfig(LoginInterceptor loginInterceptor, RefreshInterceptor refreshInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.refreshInterceptor = refreshInterceptor;
    }

    /**
     * 添加拦截器,拦截器顺序为函数调用顺序正序
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(refreshInterceptor)
                .addPathPatterns("/**");
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin/register");
    }

    /**
     * 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html")
                    .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/*.png")
//                .addResourceLocations("classpath:/static/");
                .addResourceLocations("file:"+staticResourcePath);
    }
}
