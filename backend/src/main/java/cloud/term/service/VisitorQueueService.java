package cloud.term.service;

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
}


// OG code
//@Slf4j
//public class VisitorQueueService {
//    private static final Map<String, Long> activeVisitors = new ConcurrentHashMap<>();
//    private static final int MAX_CONCURRENT_VISITORS = 1;
//    private static final long TIMEOUT_MILLIS = 10_000; // 10초 타임아웃
//
//    public static boolean isOverCapacity() {
//        return activeVisitors.size() >= MAX_CONCURRENT_VISITORS;
//    }
//
//    public static boolean isRegistered(String visitorId) {
//        return activeVisitors.containsKey(visitorId);
//    }
//
//    public static synchronized void registerVisitor(String visitorId) {
//        activeVisitors.put(visitorId, System.currentTimeMillis());
//        log.info("등록된 방문자: {}, 현재 접속자 수: {}", visitorId, activeVisitors.size());
//    }
//
//    public static synchronized void cleanupTimeoutVisitors() {
//        long now = System.currentTimeMillis();
//        activeVisitors.entrySet().removeIf(entry -> now - entry.getValue() > TIMEOUT_MILLIS);
//    }
//
//    public static synchronized void pingVisitor(String visitorId) {
//        if (activeVisitors.containsKey(visitorId)) {
//            activeVisitors.put(visitorId, System.currentTimeMillis());
//            log.info("Ping 수신 - visitorId: {}", visitorId);
//        }
//    }
//}


//
//@Slf4j
//public class VisitorQueueService {
//    private static final Map<String, Long> activeVisitors = new ConcurrentHashMap<>();
//    private static final Queue<String> waitingQueue = new ConcurrentLinkedQueue<>();
//    private static final int MAX_CONCURRENT_VISITORS = 1;
//    private static final long VISITOR_TIMEOUT_MS = 10_000;
//
//    public static synchronized void refreshVisitor(String visitorId) {
//        if (activeVisitors.containsKey(visitorId)) {
//            activeVisitors.put(visitorId, System.currentTimeMillis());
//        }
//    }
//
//    public static synchronized void registerVisitor(String visitorId) {
//
//        if (activeVisitors.containsKey(visitorId)) return;
//
//        // 대기열에 있는 사용자는 승격 대기
//        if (waitingQueue.contains(visitorId)) return;
//
//        if (activeVisitors.size() < MAX_CONCURRENT_VISITORS) {
//            activeVisitors.put(visitorId, System.currentTimeMillis());
//        } else {
//            waitingQueue.add(visitorId); // 진짜 대기자만 추가
//        }
//    }
//
//    public static synchronized void unregisterVisitor(String visitorId) {
//        activeVisitors.remove(visitorId);
//        promoteFromQueue();
//    }
//
//    private static synchronized void promoteFromQueue() {
//        if (activeVisitors.size() < MAX_CONCURRENT_VISITORS && !waitingQueue.isEmpty()) {
//            String nextVisitor = waitingQueue.poll();
//            activeVisitors.put(nextVisitor, System.currentTimeMillis());
//            log.info("대기자 승격: {}", nextVisitor);
//        }
//    }
//
//    public static synchronized void cleanupInactiveVisitors() {
//        long now = System.currentTimeMillis();
//        activeVisitors.entrySet().removeIf(entry -> (now - entry.getValue()) > VISITOR_TIMEOUT_MS);
//    }
//
//    public static synchronized int getWaitingPosition(String visitorId) {
//        int position = 0;
//        for (String id : waitingQueue) {
//            position++;
//            if (id.equals(visitorId)) return position;
//        }
//        return -1;
//    }
//
//    public static synchronized boolean isActive(String visitorId) {
//        return activeVisitors.containsKey(visitorId);
//    }
//}

