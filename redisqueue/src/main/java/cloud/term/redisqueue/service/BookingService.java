package cloud.term.redisqueue.service;

import cloud.term.redisqueue.model.BookingStatus;
import cloud.term.redisqueue.model.BookingRequestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final StringRedisTemplate redisTemplate;
    private final RedisSettingsService redisSettingsService;

    private static final String LOCK_KEY = "booking:lock";
    private static final String QUEUE_KEY = "booking:queue:list";        // 예약 요청 큐 (list)
    private static final String BOOKED_SET_KEY = "booking:booked:set";   // 예약 확정 사용자 집합 (set)
    private static final String REJECTED_SET_KEY = "booking:rejected:set"; // 예약 실패 사용자 집합 (set)
    private static final String QUEUED_SET_KEY = "booking:queued:set";   // 대기열 세트 (set)

    // 현재 예매된 사용자 수 확인
    public long getBookingCount() {
        Long size = redisTemplate.opsForSet().size(BOOKED_SET_KEY);
        return size != null ? size : 0;
    }

    // 예약 요청 처리 - 중복 및 큐 등록 처리
    public BookingRequestResult addBookingRequest(String visitorId) {
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(BOOKED_SET_KEY, visitorId))) {
            log.info("[중복 요청 거부] {} 이미 예약됨", visitorId);
            return BookingRequestResult.ALREADY_BOOKED;
        }

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(QUEUED_SET_KEY, visitorId))) {
            log.info("[중복 대기열 거부] {} 이미 대기열에 있음", visitorId);
            return BookingRequestResult.ALREADY_QUEUED;
        }

        long bookingCount = getBookingCount();
        int maxBooking = redisSettingsService.getSettingAsInt("maxBooking", 3);

        if (bookingCount >= maxBooking) {
            return BookingRequestResult.CAPACITY_FULL;
        }

        redisTemplate.opsForSet().add(QUEUED_SET_KEY, visitorId);
        redisTemplate.opsForList().rightPush(QUEUE_KEY, visitorId);
        log.info("[예약 요청 큐 등록] {}", visitorId);
        return BookingRequestResult.QUEUED_SUCCESS;
    }

    public BookingStatus getBookingStatus(String visitorId) {
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(BOOKED_SET_KEY, visitorId))) {
            return BookingStatus.BOOKED;
        }
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(QUEUED_SET_KEY, visitorId))) {
            return BookingStatus.QUEUED;
        }
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(REJECTED_SET_KEY, visitorId))) {
            return BookingStatus.REJECTED;
        }
        return BookingStatus.NOT_FOUND; // Not found in any of the specific booking sets
    }

    public String getBookingStatusasString(String visitorId) {
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(BOOKED_SET_KEY, visitorId))) {
            return BookingStatus.BOOKED.name(); // "BOOKED"
        }
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(QUEUED_SET_KEY, visitorId))) {
            return BookingStatus.QUEUED.name(); // "QUEUED"
        }
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(REJECTED_SET_KEY, visitorId))) {
            return BookingStatus.REJECTED.name(); // "REJECTED"
        }
        return BookingStatus.NOT_FOUND.name(); // "NOT_FOUND"
    }


    // 예약 요청 큐에서 하나 꺼내 예약 처리 (스케줄러용)
    public void processNextBookingRequest() {
        int lockTTL = redisSettingsService.getSettingAsInt("bookingLockTTLSeconds", 5);
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY, "1", lockTTL, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(lockAcquired)) {
            log.warn("[LOCK 대기 중] 예약 처리 중");
            return;
        }

        try {
            String visitorId = redisTemplate.opsForList().leftPop(QUEUE_KEY);

            if (visitorId == null) {
                return; // 처리할 요청 없음
            }

            // 대기열 세트에서 제거
            redisTemplate.opsForSet().remove(QUEUED_SET_KEY, visitorId);

            // 이미 예약된 사용자면 스킵
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(BOOKED_SET_KEY, visitorId))) {
                log.info("[중복 스킵] {} 이미 예약 완료", visitorId);
                return;
            }

            long bookingCount = getBookingCount();
            int maxBooking = redisSettingsService.getSettingAsInt("maxBooking", 3);

            if (bookingCount < maxBooking) {
                redisTemplate.opsForSet().add(BOOKED_SET_KEY, visitorId);
                log.info("[예약 성공] {} (현재 예약 수: {})", visitorId, bookingCount + 1);
            } else {
                redisTemplate.opsForSet().add(REJECTED_SET_KEY, visitorId);
                log.info("[예약 실패] {} (정원 초과)", visitorId);
            }
        } finally {
            redisTemplate.delete(LOCK_KEY);
        }
    }
}
