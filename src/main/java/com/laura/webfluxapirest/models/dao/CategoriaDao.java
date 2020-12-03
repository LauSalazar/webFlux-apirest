package com.laura.webfluxapirest.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.laura.webfluxapirest.models.documents.Categoria;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String>{

}
