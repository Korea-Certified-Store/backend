package com.nainga.nainga;

import com.nainga.nainga.global.config.EnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = {"classpath:env.yaml"}, factory = EnvConfig.class)
public class NaingaApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaingaApplication.class, args);
	}

}
