package com.example.demospringboot;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	Optional<Product> findFirstByOrderByPriceDesc();
}
