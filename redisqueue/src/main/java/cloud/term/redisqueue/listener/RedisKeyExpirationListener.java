/*
package cloud.term.redisqueue.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisKeyExpirationListener implements MessageListener {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String QUEUE_KEY = "visitor_queue_zset";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("[Redis] 키 만료 감지: {}", expiredKey);

        // "visitor:{visitorId}" 형태라면 visitorId 추출
        if (expiredKey.startsWith("visitor:")) {
            String visitorId = expiredKey.substring("visitor:".length());
            // 대기열에서 해당 visitorId 제거
            Long removedCount = redisTemplate.opsForZSet().remove(QUEUE_KEY, visitorId);
            if (removedCount != null && removedCount > 0) {
                log.info("[Redis] 만료된 방문자 대기열에서 제거: {}", visitorId);
            }
        }
    }
}*/