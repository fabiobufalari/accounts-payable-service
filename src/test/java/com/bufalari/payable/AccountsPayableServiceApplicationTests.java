// Path: src/test/java/com/bufalari/payable/AccountsPayableServiceApplicationTests.java
package com.bufalari.payable;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Activate H2 config for tests
class AccountsPayableServiceApplicationTests {

	@Test
	void contextLoads() {
		// Basic test to ensure the application context loads
	}

}