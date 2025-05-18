package cloud.term.controller;

import cloud.term.service.VisitorQueueService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

//@RestController
//public class QueueStatusController {
//    @GetMapping("/queue-status")
//    public Map<String, Object> checkStatus(@RequestParam String visitorId) {
//        return Map.of(
//                "active", VisitorQueueService.isActive(visitorId),
//                "position", VisitorQueueService.getWaitingPosition(visitorId),
//                "timestamp", System.currentTimeMillis()
//        );
//    }
//}