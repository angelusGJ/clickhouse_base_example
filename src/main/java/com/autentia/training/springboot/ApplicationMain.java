package com.autentia.training.springboot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ApplicationMain {



	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(ApplicationMain.class)
			.run(args);
	}
}
