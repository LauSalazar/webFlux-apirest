package com.laura.webfluxapirest;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.laura.webfluxapirest.handler.ProductoHandler;
import com.laura.webfluxapirest.models.service.ProductoService;

@Configuration
public class RouterFunctionConfig {

	@Autowired
	public ProductoService productoService;
	
	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler handler){
		return route(GET("/api/v2/productos"), handler::listar)
				.andRoute(GET("/api/v2/productos/{id}"), handler::ver)
				.andRoute(POST("/api/v2/productos"), handler::crear)
				.andRoute(PUT("/api/v2/productos/{id}"), handler::editar)
				.andRoute(DELETE("/api/v2/productos/{id}"), handler::eliminar)
				.andRoute(POST("/api/v2/productos/upload/{id}"), handler::upload)
				.andRoute(POST("/api/v2/productos/crear"), handler::createWithPhoto)
				;
	}
}
