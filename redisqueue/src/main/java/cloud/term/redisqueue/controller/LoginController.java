package cloud.term.redisqueue.controller;

import cloud.term.redisqueue.model.BookingStatus;
import cloud.term.redisqueue.service.BookingService;
import cloud.term.redisqueue.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final LoginService loginService;
    private final BookingService bookingService;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginRequest,
                                     @CookieValue(value = "visitor_id", required = false) String cookie) {

        String id = loginRequest.get("id");
        String password = loginRequest.get("password");

        if (cookie == null || cookie.isEmpty()) {
            return Map.of("status", "fail", "message", "No valid cookie provided");
        }

        boolean success = loginService.authenticate(id, password);

        if (success) {
            loginService.registerSession(id, cookie);

            BookingStatus currentBookingStatus = bookingService.getBookingStatus(id);

            Map<String, String> response = new HashMap<>();
            response.put("status", "ok");
            response.put("message", "Login successful");
            response.put("bookingStatus", currentBookingStatus.name());

            return response;
        } else {
            return Map.of("status", "fail", "message", "Invalid credentials");
        }
    }
}