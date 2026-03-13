package com.debtmanager.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"jwt.secret=dummySecretKeyForTestingPurposes"})
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
