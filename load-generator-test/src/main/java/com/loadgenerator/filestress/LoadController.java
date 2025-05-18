package com.loadgenerator.filestress;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class LoadController {

    private final WebClient webClient;
    private final AtomicInteger successCounter = new AtomicInteger(0);
    private final AtomicInteger failureCounter = new AtomicInteger(0);
    private volatile boolean isTesting = false;
    private volatile String currentTargetUrl = "";
    private volatile long currentDurationSeconds = 0;
    private volatile int currentNumberOfUsers = 500;

    public LoadController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("/")
    public String showForm() {
        return "loadTestForm";
    }

    @PostMapping("/start")
    public Mono<String> startLoadTest(@RequestParam String targetUrl,
                                      @RequestParam long duration,
                                      @RequestParam int users,
                                      Model model) {
        if (isTesting) {
            model.addAttribute("errorMessage", "현재 부하 테스트가 진행 중입니다.");
            return Mono.just("loadTestResult");
        }

        isTesting = true;
        successCounter.set(0);
        failureCounter.set(0);
        currentTargetUrl = targetUrl;
        currentDurationSeconds = duration;
        currentNumberOfUsers = users;

        return Flux.interval(Duration.ZERO, Duration.ofMillis(100))
                .take(Duration.ofSeconds(duration))
                .flatMap(tick -> Flux.range(1, users)
                        .parallel()
                        .runOn(Schedulers.boundedElastic())
                        .flatMap(user -> webClient.get()
                                .uri(targetUrl)
                                .exchangeToMono(response -> {
                                    if (response.statusCode().is2xxSuccessful()) {
                                        successCounter.incrementAndGet();
                                        return response.bodyToMono(String.class).thenReturn(true);
                                    } else {
                                        failureCounter.incrementAndGet();
                                        return response.bodyToMono(String.class).thenReturn(false);
                                    }
                                })
                                .onErrorResume(e -> {
                                    failureCounter.incrementAndGet();
                                    return Mono.just(false);
                                })
                        )
                )
                .doFinally(signalType -> isTesting = false)
                .then(Mono.fromRunnable(() -> {
                    model.addAttribute("targetUrl", targetUrl);
                    model.addAttribute("duration", duration);
                    model.addAttribute("numberOfUsers", users);
                    model.addAttribute("successCount", successCounter.get());
                    model.addAttribute("failureCount", failureCounter.get());
                }))
                .thenReturn("loadTestResult");
    }

    @GetMapping("/result")
    public String showResult(Model model) {
        model.addAttribute("targetUrl", currentTargetUrl);
        model.addAttribute("duration", currentDurationSeconds);
        model.addAttribute("numberOfUsers", currentNumberOfUsers);
        model.addAttribute("successCount", successCounter.get());
        model.addAttribute("failureCount", failureCounter.get());
        return "loadTestResult";
    }
}