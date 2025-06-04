package cloud.term.redisqueue.processor;

import cloud.term.redisqueue.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingRequestProcessor {
    private final BookingService bookingService;

    // fixedDelay 수정 가능 (현재 10000 : 10초)
    @Scheduled(fixedDelay = 10000)
    public void processVisitorQueue() {
        bookingService.processNextBookingRequest();
    }
}