package com.example.demowebflux;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
class DemowebfluxApplicationTests {

	@Autowired
	private WebfluxController controller;

	@Test
	void contextLoads() {
	}

	@Test
	void aggregateQueriesDatabaseAndReturnsSummary() {
		assertThat(controller.getAggregateData().block(Duration.ofSeconds(5)))
				.contains("Sản phẩm: 5", "Giá cao nhất: Laptop ($999.99)", "Giá trung bình: $387.69");
	}

}
