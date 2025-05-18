package cloud.term.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;

//
@Slf4j
public class VisitorCountingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();

        String visitorId = null;

        // 쿠키에서 visitor_id 찾기
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("visitor_id".equals(cookie.getName())) {
                    visitorId = cookie.getValue();
                    break;
                }
            }
        }

        // 세션에서도 한번 더 확인 (브라우저는 JSESSIONID 로 세션 유지를 시도함)
        if (visitorId == null) {
            visitorId = (String) session.getAttribute("visitor_id");
        }

        // 그래도 없으면 새로 발급 (최초 방문)
        if (visitorId == null) {
            visitorId = UUID.randomUUID().toString();
            log.info("필터에서 visitor_id 새로 발급: {}", visitorId);
        }

        // 세션에 visitor_id 저장
        session.setAttribute("visitor_id", visitorId);

        // 쿠키에도 visitor_id 세팅 (브라우저도 다음 요청부터 유지 가능하게)
        Cookie visitorCookie = new Cookie("visitor_id", visitorId);
        visitorCookie.setPath("/");
        visitorCookie.setHttpOnly(false);
        ((HttpServletResponse) response).addCookie(visitorCookie);

        chain.doFilter(request, response);
    }

}

//public class VisitorCountingFilter implements Filter {
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        String visitorId = getOrCreateVisitorId(httpRequest, httpResponse);
//        httpRequest.getSession().setAttribute("visitor_id", visitorId);
//
//        chain.doFilter(request, response);
//    }
//
//    private String getOrCreateVisitorId(HttpServletRequest request, HttpServletResponse response) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if ("visitor_id".equals(cookie.getName())) {
//                    return cookie.getValue();
//                }
//            }
//        }
//
//        String newVisitorId = UUID.randomUUID().toString();
//        Cookie cookie = new Cookie("visitor_id", newVisitorId);
//        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
//        cookie.setPath("/");
//        response.addCookie(cookie);
//        return newVisitorId;
//    }
//}





