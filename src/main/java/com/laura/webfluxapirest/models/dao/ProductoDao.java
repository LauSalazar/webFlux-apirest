package com.laura.webfluxapirest.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.laura.webfluxapirest.models.documents.Producto;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{

}
