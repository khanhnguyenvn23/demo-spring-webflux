package com.example.demospringboot;

import java.util.List;
import java.util.Locale;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MvcController {
	private final ProductRepository repository;

	public MvcController(ProductRepository repository) {
		this.repository = repository;
	}

	@GetMapping("/api/demo/blocking")
	public List<Product> getBlockingData() throws InterruptedException {
		System.out.println("[MVC] Tomcat bắt đầu phục vụ trên luồng: " + Thread.currentThread().getName());

		Thread.sleep(5000);

		System.out.println("[MVC] Trả dữ liệu về!");
		return repository.findAll();
	}

	@GetMapping("/api/demo/aggregate")
	public String getAggregateData() {
		long start = System.currentTimeMillis();

		long productCount = fetchProductCount();
		Product mostExpensive = fetchMostExpensiveProduct();
		double averagePrice = fetchAveragePrice();
		long totalTime = System.currentTimeMillis() - start;

		String result = String.format(Locale.US,
				"[Spring MVC] %d ms | Sản phẩm: %d | Giá cao nhất: %s ($%.2f) | Giá trung bình: $%.2f",
				totalTime, productCount, mostExpensive.getName(), mostExpensive.getPrice(), averagePrice);
		System.out.println(result);
		return result;
	}

	private long fetchProductCount() {
		return repository.count();
	}

	private Product fetchMostExpensiveProduct() {
		return repository.findFirstByOrderByPriceDesc()
				.orElseThrow(() -> new IllegalStateException("Không có sản phẩm trong database"));
	}

	private double fetchAveragePrice() {
		return repository.findAll().stream()
				.mapToDouble(Product::getPrice)
				.average()
				.orElse(0.0);
	}
}
