package cloud.term.redisqueue.controller;

import cloud.term.redisqueue.model.BookingRequestResult;
import cloud.term.redisqueue.service.BookingService;
import cloud.term.redisqueue.service.LoginService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {
    private final BookingService bookingService;
    private final LoginService loginService;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String visitorId = (String) session.getAttribute("visitor_id");
        model.addAttribute("visitorId", visitorId);

        boolean loggedIn = loginService.isValidCookie(visitorId);
        String loggedInId = "";
        String loggedInIdStatus = "UNKNOWN";
        if(loggedIn){
            loggedInId = loginService.getIdFromCookie(visitorId);
            loggedInIdStatus = bookingService.getBookingStatusasString(loggedInId);
        }

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
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("loggedInId", loggedInId);
        model.addAttribute("loggedInIdStatus", loggedInIdStatus);
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
        String cookie = (String) session.getAttribute("visitor_id");
        if (cookie == null || !loginService.isValidCookie(cookie)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인된 사용자만 예약할 수 있습니다.");
        }

        String userId = loginService.getIdFromCookie(cookie);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증되지 않은 쿠키입니다.");
        }

        BookingRequestResult result = bookingService.addBookingRequest(userId);

        return switch (result) {
            case ALREADY_BOOKED -> ResponseEntity.status(HttpStatus.CONFLICT).body("이미 예약이 완료된 사용자입니다.");
            case ALREADY_QUEUED -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("이미 대기열에 등록된 사용자입니다.");
            case CAPACITY_FULL -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("예약 정원이 이미 가득 찼습니다.");
            case QUEUED_SUCCESS -> ResponseEntity.ok("예매 요청이 정상적으로 접수되었습니다.");
        };
    }
}