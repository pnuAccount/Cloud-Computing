package cloud.term.redisqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RedisqueueApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisqueueApplication.class, args);
	}

}
