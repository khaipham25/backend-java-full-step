package vn.tayjava;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import vn.tayjava.controller.UserController;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BackendServiceApplicationTests {

	@Autowired
	private UserController helloController;

	@Test
	void contextLoads() {
		Assertions.assertThat(helloController).isNotNull();
	}

}
