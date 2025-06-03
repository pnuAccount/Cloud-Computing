package cloud.term.redisqueue.interceptor;

//import cloud.term.redisqueue.service.VisitorQueueService;
import cloud.term.redisqueue.service.RedisVisitorQueueService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class TrafficLimitInterceptor implements HandlerInterceptor {

//    private final int capacityThreshold;
    private final RedisVisitorQueueService redisVisitorQueueService;

    public TrafficLimitInterceptor(int capacityThreshold, RedisVisitorQueueService redisVisitorQueueService) {
 //       this.capacityThreshold = capacityThreshold;
        this.redisVisitorQueueService = redisVisitorQueueService;
    }

    /*
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (request.getRequestURI().equals("/ping")) {
            return true;
        }

        HttpSession session = request.getSession();
        String visitorId = (String) session.getAttribute("visitor_id");

        if (visitorId == null) {
            return true;
        }

        // 메모리 기반 cleanup (정상 접속자)
        VisitorQueueService.cleanupTimeoutVisitors();

        // 접속자 수 초과 & 아직 입장하지 않은 사용자라면
        if (VisitorQueueService.isOverCapacity(capacityThreshold) && !VisitorQueueService.isRegistered(visitorId)) {
            log.warn("최대 접속자 초과 - 방문자 {} fallback 처리 및 Redis 등록", visitorId);

            // fallback 대상 방문자만 Redis에 등록 혹은 TTL 갱신
            redisVisitorQueueService.enqueue(visitorId);

            // fallback 페이지로 보내기
            request.getRequestDispatcher("/fallback").forward(request, response);

            return false;
        }

 //       redisVisitorQueueService.refresh(visitorId); // TTL 연장


        // 정상 방문자 메모리 기반 등록 유지
        VisitorQueueService.registerVisitor(visitorId);

        return true;
    }*/
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






/*
package cloud.term.redisqueue.interceptor;

import cloud.term.redisqueue.service.VisitorQueueService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class TrafficLimitInterceptor implements HandlerInterceptor {

    private final int capacityThreshold;

    public TrafficLimitInterceptor(int capacityThreshold) {
        this.capacityThreshold = capacityThreshold;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 정적 리소스 또는 핑 요청은 bypass
        if (request.getRequestURI().equals("/ping")) {
            return true;
        }

        HttpSession session = request.getSession();
        String visitorId = (String) session.getAttribute("visitor_id");

        if (visitorId == null) {
            return true;
        }

        VisitorQueueService.cleanupTimeoutVisitors();

        if (VisitorQueueService.isOverCapacity(capacityThreshold) && !VisitorQueueService.isRegistered(visitorId)) {
            log.warn("최대 접속자 초과 - 방문자 {} 대기페이지로 이동", visitorId);
//            response.sendRedirect("/fallback");
            request.getRequestDispatcher("/fallback").forward(request, response); // 수정 함
            return false;
        }

        VisitorQueueService.registerVisitor(visitorId);
        return true;
    }
}

*/