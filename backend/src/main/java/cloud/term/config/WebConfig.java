package cloud.term.config;

import cloud.term.filter.VisitorCountingFilter;
import cloud.term.interceptor.TrafficLimitInterceptor;
import cloud.term.service.VisitorQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TrafficLimitInterceptor())
                .addPathPatterns("/")
                .excludePathPatterns("/favicon.ico", "/css/**", "/js/**", "/images/**", "/static/**", "/ping");
    }

    @Bean
    public FilterRegistrationBean<VisitorCountingFilter> visitorCountingFilter() {
        FilterRegistrationBean<VisitorCountingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new VisitorCountingFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}


//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new TrafficLimitInterceptor())
//                .addPathPatterns("/");
//    }
//
//    @Bean
//    public FilterRegistrationBean<VisitorCountingFilter> visitorCountingFilter() {
//        FilterRegistrationBean<VisitorCountingFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(new VisitorCountingFilter());
//        registration.addUrlPatterns("/*");
//        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
//        return registration;
//    }
//}