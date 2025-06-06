package cloud.term.redisqueue.controller;

import cloud.term.redisqueue.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final LoginService loginService;

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
            return Map.of("status", "ok", "message", "Login successful");
        } else {
            return Map.of("status", "fail", "message", "Invalid credentials");
        }
    }
}