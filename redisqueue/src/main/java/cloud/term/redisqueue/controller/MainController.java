package cloud.term.redisqueue.controller;

//import cloud.term.redisqueue.service.VisitorQueueService;
import cloud.term.redisqueue.model.BookingRequestResult;
import cloud.term.redisqueue.service.BookingService;
import cloud.term.redisqueue.service.RedisVisitorQueueService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
@RequiredArgsConstructor
public class MainController {
    private final RedisVisitorQueueService redisVisitorQueueService;
    private final BookingService bookingService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String visitorId = (String) session.getAttribute("visitor_id");
        model.addAttribute("visitorId", visitorId);

        String serverlocalHostIpAddress;
        String serverlocalHostName;
        try {
            InetAddress serverlocalHost = InetAddress.getLocalHost();
            serverlocalHostIpAddress= serverlocalHost.getHostAddress();
            serverlocalHostName= serverlocalHost.getHostName();
        } catch(UnknownHostException e){
            serverlocalHostIpAddress = "Error id";
            serverlocalHostName = "Error name";
        }
        model.addAttribute("serverIp", serverlocalHostIpAddress);
        model.addAttribute("serverName", serverlocalHostName);
        return "ticketing";
    }

    @GetMapping("/fallback")
    public String fallback(Model model, HttpSession session) {
        String visitorId = (String) session.getAttribute("visitor_id");
        model.addAttribute("visitorId", visitorId != null ? visitorId : "N/A");
        return "fallback";
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck(){
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PostMapping("/apply")
    public ResponseEntity<String> apply(HttpSession session) {
        String visitorId = (String) session.getAttribute("visitor_id");

        if (visitorId == null) {
            return ResponseEntity.badRequest().body("방문자 ID가 없습니다.");
        }

        BookingRequestResult result = bookingService.addBookingRequest(visitorId);

        return switch (result) {
            case ALREADY_BOOKED -> ResponseEntity.status(HttpStatus.CONFLICT).body("이미 예약이 완료된 사용자입니다.");
            case ALREADY_QUEUED -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("이미 대기열에 등록된 사용자입니다.");
            case CAPACITY_FULL -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("예약 정원이 이미 가득 찼습니다.");
            case QUEUED_SUCCESS -> ResponseEntity.ok("예매 요청이 정상적으로 접수되었습니다.");
        };
    }
/*
    @PostMapping("/ping")
    @ResponseBody
    public String ping(HttpSession session) {
        String visitorId = (String) session.getAttribute("visitor_id");
        if (visitorId != null) {
            VisitorQueueService.pingVisitor(visitorId);
        }
        return "pong";
    }

 */
}
