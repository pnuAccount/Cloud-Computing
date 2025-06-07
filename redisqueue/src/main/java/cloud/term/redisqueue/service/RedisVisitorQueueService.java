package cloud.term.redisqueue.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor
public class RedisVisitorQueueService {

    private final StringRedisTemplate redisTemplate;
    private final RedisSettingsService redisSettingsService;

    private static final String VISITOR_KEY_PREFIX = "visitor:";

    // 방문자 TTL 저장 (중복 방지용)
    public void saveVisitorIfNotExists(String visitorId) {
        String key = VISITOR_KEY_PREFIX + visitorId;
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.FALSE.equals(exists)) {
            int visitorTTL = redisSettingsService.getSettingAsInt("visitorTTLMinutes", 10);
            redisTemplate.opsForValue().set(key, "1", visitorTTL, TimeUnit.MINUTES);
        }
    }
}