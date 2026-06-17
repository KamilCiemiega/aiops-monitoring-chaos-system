package pl.kamil.aiops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MonitoringChaosSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitoringChaosSystemApplication.class, args);
	}

}
