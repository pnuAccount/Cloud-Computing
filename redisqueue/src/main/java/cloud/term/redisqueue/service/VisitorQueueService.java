/*
package cloud.term.redisqueue.service;

import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.*;

@Slf4j
public class VisitorQueueService {
    private static final Map<String, Long> activeVisitors = new ConcurrentHashMap<>();
    private static final long VISITOR_TIMEOUT = 10_000; // 10초

    public static boolean isOverCapacity(int threshold) {
        return activeVisitors.size() >= threshold;
    }

    public static boolean isRegistered(String visitorId) {
        return activeVisitors.containsKey(visitorId);
    }

    public static void registerVisitor(String visitorId) {
        activeVisitors.put(visitorId, System.currentTimeMillis());
        log.debug("방문자 등록: {}, 현재 접속자: {}", visitorId, activeVisitors.size());
    }

    public static void cleanupTimeoutVisitors() {
        long now = System.currentTimeMillis();
        activeVisitors.entrySet().removeIf(entry -> (now - entry.getValue()) > VISITOR_TIMEOUT);
    }

    public static void pingVisitor(String visitorId) {
        if (activeVisitors.containsKey(visitorId)) {
            activeVisitors.put(visitorId, System.currentTimeMillis());
        }
    }

    // 현재 활성 방문자 수 반환
    public static int getActiveVisitorCount() {
        cleanupTimeoutVisitors();
        return activeVisitors.size();
    }

}
*/