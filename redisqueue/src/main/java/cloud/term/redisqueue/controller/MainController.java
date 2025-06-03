package cloud.term.redisqueue.controller;

//import cloud.term.redisqueue.service.VisitorQueueService;
import jakarta.servlet.http.HttpSession;
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
public class MainController {

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String visitorId = (String) session.getAttribute("visitor_id");
        model.addAttribute("visitorId", visitorId);

        String serverlocalHostIpAddress;
        try {
            InetAddress serverlocalHost = InetAddress.getLocalHost();
            serverlocalHostIpAddress= serverlocalHost.getHostAddress();
        } catch(UnknownHostException e){
            serverlocalHostIpAddress = "Error id";
        }
        model.addAttribute("serverIp", serverlocalHostIpAddress);
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
