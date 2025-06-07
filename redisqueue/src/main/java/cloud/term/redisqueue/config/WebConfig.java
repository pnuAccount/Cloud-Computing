package cloud.term.redisqueue.config;

import cloud.term.redisqueue.filter.VisitorCountingFilter;
import cloud.term.redisqueue.interceptor.TrafficLimitInterceptor;
import cloud.term.redisqueue.service.RedisVisitorQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RedisVisitorQueueService redisVisitorQueueService;

    @Bean
    public TrafficLimitInterceptor trafficLimitInterceptor(){
        int capacityThreshold = 1;
        return new TrafficLimitInterceptor(capacityThreshold, redisVisitorQueueService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trafficLimitInterceptor()) // <-- () 메서드 호출해야 합니다!
                .addPathPatterns("/", "/ticketing")
                .excludePathPatterns("/favicon.ico", "/css/**", "/js/**", "/images/**", "/static/**", "/ping", "/fallback");
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

