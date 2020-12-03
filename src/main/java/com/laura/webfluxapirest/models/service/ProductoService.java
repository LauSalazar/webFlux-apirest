package com.laura.webfluxapirest.models.service;

import com.laura.webfluxapirest.models.documents.Categoria;
import com.laura.webfluxapirest.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {
	 public Flux<Producto> findAll();
	 public Mono<Producto> findById(String id);
	 public Mono<Producto> save(Producto producto);
	 public Mono<Void> delete(Producto producto);
	 public Flux<Producto> findAllNameUpperCase();
	 public Flux<Producto> findAllNameUpperCaseRepeat();
	 public Flux<Categoria> findAllCategoria();
	 public Mono<Categoria> findCategoriaById(String id);
	 public Mono<Categoria> save(Categoria categoria);
}
