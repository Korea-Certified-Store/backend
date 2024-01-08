package com.nainga.nainga;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"GOOGLE_API_KEY"})
@SpringBootTest
class NaingaApplicationTests {

	@Test
	void contextLoads() {
	}

}
