package cloud.term.controller;

import cloud.term.service.VisitorQueueService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
public class MainController {

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String visitorId = (String) session.getAttribute("visitor_id");
        model.addAttribute("visitorId", visitorId);
        return "ticketing";
    }

    @GetMapping("/fallback")
    public String fallback(Model model, HttpSession session) {
        String visitorId = (String) session.getAttribute("visitor_id");
        model.addAttribute("visitorId", visitorId != null ? visitorId : "N/A");
        return "fallback";
    }


    @PostMapping("/ping")
    @ResponseBody
    public String ping(HttpSession session) {
        String visitorId = (String) session.getAttribute("visitor_id");
        if (visitorId != null) {
            VisitorQueueService.pingVisitor(visitorId);
        }
        return "pong";
    }
}

// OG code
//@Slf4j
//@Controller
//public class MainController {
//    @GetMapping("/")
//    public String home(@CookieValue(value = "visitor_id", required = false) String visitorId, HttpServletResponse response, Model model, HttpSession session) {
//        if (visitorId == null) {
//            visitorId = UUID.randomUUID().toString();
//            Cookie cookie = new Cookie("visitor_id", visitorId);
//            cookie.setPath("/");
//            cookie.setHttpOnly(true);
//            cookie.setSecure(false);
//            response.addCookie(cookie);
//            log.info("새로운 방문자 UUID 생성: {}", visitorId);
//        }
//
//        if (session.getAttribute("visitor_id") == null) {
//            session.setAttribute("visitor_id", visitorId);
//        }
//
//        model.addAttribute("visitorId", visitorId);
//        return "ticketing";
//    }
//
//    @GetMapping("/fallback")
//    public String fallback(@CookieValue(value = "visitor_id", required = false) String visitorId, Model model) {
//        model.addAttribute("visitorId", visitorId != null ? visitorId : "N/A");
//        return "fallback";
//    }
//
//    @PostMapping("/ping")
//    @ResponseBody
//    public String ping(@CookieValue(value = "visitor_id", required = false) String visitorId) {
//        if (visitorId != null) {
//            VisitorQueueService.pingVisitor(visitorId);
//        }
//        return "pong";
//    }
//}


//@Controller
//public class MainController {
//    @GetMapping("/")
//    public String home(@CookieValue("visitor_id") String visitorId, Model model) {
//        model.addAttribute("visitorId", visitorId);
//        return "ticketing";
//    }
//
//    @GetMapping("/fallback")
//    public String fallback(@RequestParam String visitorId, Model model) {
//        model.addAttribute("visitorId", visitorId);
//        return "fallback";
//    }
//
//    @PostMapping("/exit")
//    public String exit(@CookieValue("visitor_id") String visitorId) {
//        VisitorQueueService.unregisterVisitor(visitorId);
//        return "redirect:/";
//    }
//}