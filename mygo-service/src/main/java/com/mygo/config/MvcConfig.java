package com.mygo.config;

import com.mygo.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    /**注意，要实现WebMvcConfigurer这个接口。这样的话，只会在原来的配置上根据这个配置类修改。<br>
    * 如果继承了WebMvcConfigurationSupport这个抽象类，会覆盖原来所有的配置。
    * */

    private final LoginInterceptor loginInterceptor;


    @Value("${mygo.static.img}")
    private String staticResourcePath;

    @Autowired
    public MvcConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    /**
     * 添加拦截器,拦截器顺序为函数调用顺序正序
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/admin/**", "/user/**")
                .excludePathPatterns(
                    "/admin/login",
                    "/admin/register",
                    "/user/login",
                    "/user/register"
                );
    }

    /**
     * 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*.png")
                .addResourceLocations("file:" + staticResourcePath);
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")  // 允许所有来源，或者列出所有前端URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .maxAge(3600);
    }

}
