package com.nainga.nainga;

import com.nainga.nainga.domain.store.application.GoogleMapStoreService;
import com.nainga.nainga.domain.store.application.MobeomDataParser;
import com.nainga.nainga.global.config.EnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = {"classpath:env.yml"}, factory = EnvConfig.class)
public class NaingaApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaingaApplication.class, args);
	}

}
