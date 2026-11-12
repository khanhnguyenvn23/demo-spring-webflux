package com.example.demowebflux;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class WebfluxController {

	private final ProductRepository repository;

	public WebfluxController(ProductRepository repository) {
		this.repository = repository;
	}

	@GetMapping(value = "/api/demo/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> streamData() {
		return repository.findAll()
				.delayElements(Duration.ofSeconds(1))
				.map(product -> String.format(Locale.US, "[%s] Trích xuất: %s - $%.2f",
						LocalTime.now(), product.getName(), product.getPrice()));
	}

	@GetMapping("/api/demo/non-blocking")
	public Mono<List<Product>> getNonBlockingData() {
		return repository.findAll()
				.collectList()
				.delayElement(Duration.ofSeconds(5))
				.doOnNext(products -> System.out.println(
						"[WebFlux] Netty trả kết quả trên luồng: " + Thread.currentThread().getName()));
	}

	@GetMapping("/api/demo/aggregate")
	public Mono<String> getAggregateData() {
		long start = System.currentTimeMillis();

		Mono<Long> productCountMono = fetchProductCount();
		Mono<Product> mostExpensiveMono = fetchMostExpensiveProduct();
		Mono<Double> averagePriceMono = fetchAveragePrice();

		return Mono.zip(productCountMono, mostExpensiveMono, averagePriceMono)
				.map(tuple -> {
					long totalTime = System.currentTimeMillis() - start;
					return String.format(Locale.US,
							"[WebFlux] %d ms | Sản phẩm: %d | Giá cao nhất: %s ($%.2f) | Giá trung bình: $%.2f",
							totalTime, tuple.getT1(), tuple.getT2().getName(), tuple.getT2().getPrice(),
							tuple.getT3());
				})
				.doOnNext(System.out::println);
	}

	private Mono<Long> fetchProductCount() {
		return repository.count();
	}

	private Mono<Product> fetchMostExpensiveProduct() {
		return repository.findFirstByOrderByPriceDesc()
				.switchIfEmpty(Mono.error(new IllegalStateException("Không có sản phẩm trong database")));
	}

	private Mono<Double> fetchAveragePrice() {
		return repository.findAll()
				.collectList()
				.map(products -> products.stream()
						.mapToDouble(Product::getPrice)
						.average()
						.orElse(0.0));
	}
}
