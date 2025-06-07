package cloud.term.redisqueue.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class VisitorCountingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession();

        String visitorId = getVisitorIdFromCookies(httpRequest);

        if (visitorId == null) {
            visitorId = (String) session.getAttribute("visitor_id");
        }

        if (visitorId == null) {
            visitorId = UUID.randomUUID().toString();
            log.info("새로운 방문자 ID 발급: {}", visitorId);
            setVisitorCookie(httpResponse, visitorId);
        }

        session.setAttribute("visitor_id", visitorId);

        chain.doFilter(request, response);
    }

    private String getVisitorIdFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("visitor_id".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setVisitorCookie(HttpServletResponse response, String visitorId) {
        Cookie cookie = new Cookie("visitor_id", visitorId);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(false); // HTTPS 사용 시
        cookie.setMaxAge(60 * 60); // 1 시간 유지
        response.addCookie(cookie);
    }
}

