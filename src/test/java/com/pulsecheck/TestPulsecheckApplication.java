package com.pulsecheck;

import org.springframework.boot.SpringApplication;

public class TestPulsecheckApplication {

	public static void main(String[] args) {
		SpringApplication.from(PulsecheckApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
