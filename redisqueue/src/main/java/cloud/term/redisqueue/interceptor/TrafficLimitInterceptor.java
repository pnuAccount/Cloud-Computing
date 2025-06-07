package cloud.term.redisqueue.interceptor;

import cloud.term.redisqueue.service.RedisVisitorQueueService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
public class TrafficLimitInterceptor implements HandlerInterceptor {

    private final RedisVisitorQueueService redisVisitorQueueService;

    public TrafficLimitInterceptor(int capacityThreshold, RedisVisitorQueueService redisVisitorQueueService) {
        this.redisVisitorQueueService = redisVisitorQueueService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String visitorId = (String) session.getAttribute("visitor_id");

        if (visitorId != null) {
            redisVisitorQueueService.saveVisitorIfNotExists(visitorId);
        }
        return true; // 아무런 제한 없이 계속 진행
    }

}