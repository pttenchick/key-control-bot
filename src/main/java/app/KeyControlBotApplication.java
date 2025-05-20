package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KeyControlBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(KeyControlBotApplication.class, args);
    }
}
