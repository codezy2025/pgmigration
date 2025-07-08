package com.java.b2p.pgmigration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

	@SpringBootApplication
	@EnableScheduling
	public class PgmigrationApplication {
		public static void main(String[] args) {
			SpringApplication.run(PgmigrationApplication.class, args);
		}
	}