package cloud.term.redisqueue.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultSettingsInitializer {

    private final StringRedisTemplate redisTemplate;

    private static final String SETTINGS_KEY = "booking:settings";

    @PostConstruct
    public void init() {
        try {
            setDefaultIfAbsent("maxBooking", "3");
            setDefaultIfAbsent("visitorTTLMinutes", "10");
            setDefaultIfAbsent("bookingLockTTLSeconds", "5");
        } catch (Exception e) {
            System.err.println("Redis 기본 설정 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setDefaultIfAbsent(String field, String defaultValue) {
        Boolean exists = redisTemplate.opsForHash().hasKey(SETTINGS_KEY, field);
        if (Boolean.FALSE.equals(exists)) {
            redisTemplate.opsForHash().put(SETTINGS_KEY, field, defaultValue);
        }
    }
}