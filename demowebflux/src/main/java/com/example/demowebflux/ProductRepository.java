package com.example.demowebflux;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveCrudRepository<Product, Integer> {
	Mono<Product> findFirstByOrderByPriceDesc();
}
