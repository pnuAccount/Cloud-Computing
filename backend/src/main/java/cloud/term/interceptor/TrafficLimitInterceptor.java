package cloud.term.interceptor;

import cloud.term.service.VisitorQueueService;
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

//
@Slf4j
public class TrafficLimitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getRequestURI().equals("/")) {
            return true;
        }

        HttpSession session = request.getSession();
        String visitorId = (String) session.getAttribute("visitor_id");

        if (visitorId == null) {
            return true; // 쿠키 생성은 컨트롤러에서
        }

        VisitorQueueService.cleanupTimeoutVisitors();

        if (VisitorQueueService.isOverCapacity() && !VisitorQueueService.isRegistered(visitorId)) {
            log.warn("최대 접속자 초과 - 방문자 {} 대기열로 이동", visitorId);
            response.sendRedirect("/fallback");
            return false;
        }

        VisitorQueueService.registerVisitor(visitorId);
        return true;
    }
}


//public class TrafficLimitInterceptor implements HandlerInterceptor {
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//        String visitorId = (String) request.getSession().getAttribute("visitor_id");
//        if (visitorId == null) return true;
//
//        // 재접속 사용자 검증
//        if (VisitorQueueService.isActive(visitorId)) {
//            return true;
//        }
//
//        VisitorQueueService.registerVisitor(visitorId);
//
//        if (!VisitorQueueService.isActive(visitorId)) {
//            response.sendRedirect("/fallback?visitorId=" + visitorId);
//            return false;
//        }
//        return true;
//    }
//}
