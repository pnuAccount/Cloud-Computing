package cloud.term.redisqueue.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSettingsService {
    private final StringRedisTemplate redisTemplate;
    private HashOperations<String, String, String> hashOps;
    private static final String SETTINGS_KEY = "booking:settings";
    private volatile Map<String, String> cachedSettings = new HashMap<>();

    @PostConstruct
    private void init() {
        hashOps = redisTemplate.opsForHash();
        loadSettingsFromRedis();
    }

    @Scheduled(fixedDelayString = "${settings.refresh.interval.ms:30000}")
    public void loadSettingsFromRedis() {
        try {
            Map<String, String> settings = hashOps.entries(SETTINGS_KEY);
            if (settings != null && !settings.isEmpty()) {
                cachedSettings = settings;
                log.info("설정 캐시 업데이트: {}", cachedSettings);
            } else {
                log.warn("Redis에서 설정값을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("Redis에서 설정 로딩 실패", e);
        }
    }

    public String getSetting(String key) {
        return cachedSettings.get(key);
    }

    public Map<String, String> getAllSettings() {
        return new HashMap<>(cachedSettings);
    }

    public void updateSetting(String key, String value) {
        try {
            int intValue = Integer.parseInt(value);
            if (intValue <= 0) {
                log.warn("설정 업데이트 실패 - '{}' 값은 양수여야 합니다: {}", key, value);
                return;
            }

            hashOps.put(SETTINGS_KEY, key, value);
            cachedSettings.put(key, value);
            log.info("설정 '{}' 이(가) '{}' 로 업데이트되었습니다.", key, value);
        } catch (NumberFormatException e) {
            log.warn("설정 업데이트 실패 - '{}' 값이 정수가 아닙니다: {}", key, value);
        }
    }

    public int getSettingAsInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getSetting(key));
        } catch (Exception e) {
            log.warn("{} 값 파싱 실패. 기본값({}) 사용", key, defaultValue);
            return defaultValue;
        }
    }
}