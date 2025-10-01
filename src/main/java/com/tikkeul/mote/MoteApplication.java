package com.tikkeul.mote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoteApplication.class, args);
	}

}
