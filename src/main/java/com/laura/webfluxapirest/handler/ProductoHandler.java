package com.laura.webfluxapirest.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.BodyInserters.*;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import com.laura.webfluxapirest.models.documents.Categoria;
import com.laura.webfluxapirest.models.documents.Producto;
import com.laura.webfluxapirest.models.service.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

	@Autowired
	ProductoService productoService;
	
	@Value("{config.uploads.path}")
	private String path;
	
	@Autowired
	private Validator validator;
	
	public Mono<ServerResponse> createWithPhoto(ServerRequest request){

		Mono<Producto> producto = request.multipartData().map(multipartdata -> {
			FormFieldPart nombre = (FormFieldPart) multipartdata.toSingleValueMap().get("nombre");
			FormFieldPart precio = (FormFieldPart) multipartdata.toSingleValueMap().get("precio");
			FormFieldPart categoriaId = (FormFieldPart) multipartdata.toSingleValueMap().get("categoria.id");
			FormFieldPart categoriaNombre = (FormFieldPart) multipartdata.toSingleValueMap().get("categoria.nombre");
			Categoria cat = new Categoria();
			cat.setId(categoriaId.value());
			cat.setNombre(categoriaNombre.value());
			return new Producto(nombre.value(), Double.parseDouble(precio.value()), cat);
		});
		return request.multipartData().map( multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> producto
						.flatMap(p -> {
					p.setFoto(UUID.randomUUID().toString()+"-"+file.filename()
					.replace(" ", "")
					.replace(":", "")
					.replace("//", ""));
					p.setCreateAt(new Date());
					return file.transferTo(new File(path + p.getFoto())).then(productoService.save(p));
				})).flatMap(pro -> ServerResponse.created(URI.create("/api/v2/productos".concat(pro.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(pro)));
	}
	
	public Mono<ServerResponse> upload(ServerRequest request){
		String id = request.pathVariable("id");
		return request.multipartData().map( multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> productoService.findById(id)
						.flatMap(p -> {
					p.setFoto(UUID.randomUUID().toString()+"-"+file.filename()
					.replace(" ", "")
					.replace(":", "")
					.replace("//", ""));
					return file.transferTo(new File(path + p.getFoto())).then(productoService.save(p));
				})).flatMap(pro -> ServerResponse.created(URI.create("/api/v2/productos".concat(pro.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(pro)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> listar(ServerRequest request){
		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(productoService.findAll(), Producto.class);
	}
	
	public Mono<ServerResponse> ver(ServerRequest request){
		String id = request.pathVariable("id");
		return productoService.findById(id).flatMap(p -> 
			ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(fromValue(p))
					.switchIfEmpty(ServerResponse.notFound().build()));
	}
	
	public Mono<ServerResponse> crear(ServerRequest request){
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		return producto.flatMap(p -> {
			Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
			validator.validate(p, errors);
			
			if(errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
						.map(fieldError -> "El campo "+fieldError.getField()+" "+fieldError.getDefaultMessage())
						.collectList()
						.flatMap(list -> ServerResponse.badRequest().body(fromValue(list)));
			}else {
				if(p.getCreateAt() == null) {
					p.setCreateAt(new Date());
				}
				return productoService.save(p)
						.flatMap( pro -> ServerResponse.created(URI.create("/api/v2/productos".concat(pro.getId())))
								.contentType(MediaType.APPLICATION_JSON)
								.body(fromValue(pro)));
			}
		});
	}
	
	public Mono<ServerResponse> editar(ServerRequest request){
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");
		Mono<Producto> productodb=productoService.findById(id);
		
		return productodb.zipWith(producto, (db, req) -> {
			db.setNombre(req.getNombre());
			db.setPrecio(req.getPrecio());
			db.setCategoria(req.getCategoria());
			return db;
		}).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(productoService.save(p), Producto.class))
		  .switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> eliminar(ServerRequest request){
		String id = request.pathVariable("id");	
		Mono<Producto> productobd = productoService.findById(id);
		
		return productobd.flatMap(p -> productoService.delete(p).then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
}
