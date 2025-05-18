package cloud.term.config;

import cloud.term.service.VisitorQueueService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

//@Configuration
//@EnableScheduling
//public class ScheduleConfig {
//    @Scheduled(fixedRate = 5_000)
//    public void checkInactiveVisitors() {
//        VisitorQueueService.cleanupInactiveVisitors();
//    }
//}