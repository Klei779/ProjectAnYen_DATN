package vn.anyen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnyenBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnyenBackendApplication.class, args);
	}

}
