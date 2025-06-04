package cloud.term.redisqueue.service;

import io.lettuce.core.AbstractRedisAsyncCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor
public class RedisVisitorQueueService {

    private final StringRedisTemplate redisTemplate;

    private static final String VISITOR_KEY_PREFIX = "visitor:";
    private static final String QUEUED_SET_KEY = "booking:queued:set";

    // 방문자 TTL 저장 (중복 방지용)
    public void saveVisitorIfNotExists(String visitorId) {
        String key = VISITOR_KEY_PREFIX + visitorId;
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.FALSE.equals(exists)) {
            redisTemplate.opsForValue().set(key, "1", 10, TimeUnit.MINUTES);
        }
    }

    // 대기열 세트에 포함 여부
    public boolean isVisitorInQueue(String visitorId) {
        Boolean result = redisTemplate.opsForSet().isMember(QUEUED_SET_KEY, visitorId);
        return Boolean.TRUE.equals(result);
    }

    // 대기열 세트에 추가
    public void addToQueuedSet(String visitorId) {
        redisTemplate.opsForSet().add(QUEUED_SET_KEY, visitorId);
    }

    // 대기열 세트에서 제거
    public void removeFromQueuedSet(String visitorId) {
        redisTemplate.opsForSet().remove(QUEUED_SET_KEY, visitorId);
    }
}


/*
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisVisitorQueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String QUEUE_KEY = "visitor_queue_zset";
    private static final Duration TTL = Duration.ofSeconds(10);

    private String keyForVisitor(String visitorId) {
        return "visitor:" + visitorId;
    }

  //  /**
   //  * 방문자 대기열에 등록 (큐에 넣고 개별 key에 TTL 설정)
  //   *
    public void enqueue(String visitorId) {
        String key = keyForVisitor(visitorId);


        // queue(ZSET)에 등록은 처음 한 번만
        boolean alreadyQueued = redisTemplate.opsForZSet().rank(QUEUE_KEY, visitorId) != null;
        if (!alreadyQueued) {
            long score = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(QUEUE_KEY, visitorId, score);
            redisTemplate.opsForValue().set(key, "1", TTL); // 신규 등록 + TTL 설정
            log.debug("[QUEUE] 방문자 {} 대기열 최초 등록", visitorId);
        } else {
            // 기존 키가 있으면 TTL 갱신
            redisTemplate.expire(key, TTL);

            log.debug("[QUEUE] 방문자 {} TTL 갱신", visitorId);
        }
    }

    /**
    // * 대기열 선두 방문자 확인*
    // *
    public String peek() {
        Set<String> range = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, 0);
        return (range != null && !range.isEmpty()) ? range.iterator().next() : null;
    }

  //  /**
  //   * 방문자가 입장 가능한 상태인지 확인 (순위가 허용 범위 내인지)
 //    *
    public boolean isAllowed(String visitorId, int allowedCount) {
        Long rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, visitorId);
        return rank != null && rank < allowedCount;
    }

 //   /**
 //    * 방문자 큐에서 제거 (예: 입장 허용 후 또는 만료 시)
//     *

    public void dequeue(String visitorId) {
        redisTemplate.opsForZSet().remove(QUEUE_KEY, visitorId);
        redisTemplate.delete(keyForVisitor(visitorId));
        log.debug("[QUEUE] 방문자 {} 대기열 제거", visitorId);
    }

 //   /**
 //    * 현재 대기열 길이
 //    *

    public long getQueueSize() {
        Long size = redisTemplate.opsForZSet().size(QUEUE_KEY);
        return size != null ? size : 0;
    }
}
*/


/*package cloud.term.redisqueue.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisVisitorQueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String QUEUE_KEY = "visitor_queue_zset";
    private static final Duration TTL = Duration.ofSeconds(10);

    private String keyForVisitor(String visitorId) {
        return "visitor:" + visitorId;
    }

   /// /**
   ///  * 방문자 대기열에 등록
    /// *
    public void enqueue(String visitorId) {
        String key = keyForVisitor(visitorId);

        // 이미 등록되어 있으면 skip
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) return;

        long score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(QUEUE_KEY, visitorId, score);
        redisTemplate.opsForValue().set(key, "1", TTL);
        log.debug("[QUEUE] 방문자 {} 대기열 등록", visitorId);
    }

  ///  /**
   ///  * 대기열 선두 방문자 확인
   ///  *
    public String peek() {
        Set<String> top = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, 0);
        return (top != null && !top.isEmpty()) ? top.iterator().next() : null;
    }

  ///  /**
    /// * 방문자가 입장 가능한 상태인지 확인
  ////   *
    public boolean isAllowed(String visitorId, int allowedCount) {
        Long rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, visitorId);
        return rank != null && rank < allowedCount;
    }

  ///  /**
   ///  * 방문자 갱신 (TTL 초기화)
   ///  *
    public void refresh(String visitorId) {
        String key = keyForVisitor(visitorId);
        redisTemplate.expire(key, TTL);
    }

 ///   /**
  ///   * 타임아웃된 방문자 정리
///     *
    public void cleanupExpiredVisitors() {
        Set<String> all = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, -1);
        if (all == null) return;

        for (String visitorId : all) {
            if (Boolean.FALSE.equals(redisTemplate.hasKey(keyForVisitor(visitorId)))) {
                redisTemplate.opsForZSet().remove(QUEUE_KEY, visitorId);
                log.debug("[CLEANUP] 만료된 방문자 제거: {}", visitorId);
            }
        }
    }

   /// /**
  ///   * 현재 대기열 길이
  ///   *
    public long getQueueSize() {
        return redisTemplate.opsForZSet().size(QUEUE_KEY);
    }
}
*/
/*
package cloud.term.redisqueue.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisVisitorQueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String QUEUE_KEY = "visitor_queue";
    private static final String VISITOR_SET = "active_visitors";
    private static final long VISITOR_TIMEOUT_SEC = 10L;

    // 방문자 등록 (ZSet에 타임스탬프와 함께 저장)
    public void registerVisitor(String visitorId) {
        long now = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(VISITOR_SET, visitorId, now);
        redisTemplate.expire(VISITOR_SET, Duration.ofSeconds(VISITOR_TIMEOUT_SEC));
        log.debug("Redis 방문자 등록: {}", visitorId);
    }

    // 방문자 대기열에 추가
    public void enqueueVisitor(String visitorId) {
        redisTemplate.opsForList().rightPush(QUEUE_KEY, visitorId);
        log.info("Redis 대기열에 방문자 추가: {}", visitorId);
    }

    // 방문자 등록 여부 확인
    public boolean isRegistered(String visitorId) {
        Double score = redisTemplate.opsForZSet().score(VISITOR_SET, visitorId);
        return score != null;
    }

    // 오래된 방문자 제거 (타임아웃)
    public void cleanupTimeoutVisitors() {
        long threshold = System.currentTimeMillis() - VISITOR_TIMEOUT_SEC * 1000;
        redisTemplate.opsForZSet().removeRangeByScore(VISITOR_SET, 0, threshold);
        log.debug("Redis 오래된 방문자 제거");
    }
}*/