package com.example.demospringboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemospringbootApplicationTests {

	@Autowired
	private ProductRepository repository;

	@Autowired
	private MvcController controller;

	@Test
	void contextLoads() {
	}

	@Test
	void loadsProductsFromSchema() {
		assertThat(repository.findAll()).hasSize(5);
	}

	@Test
	void aggregateQueriesDatabaseAndReturnsSummary() {
		assertThat(controller.getAggregateData())
				.contains("Sản phẩm: 5", "Giá cao nhất: MacBook Air M3 ($1200.00)", "Giá trung bình: $789.40");
	}

}
