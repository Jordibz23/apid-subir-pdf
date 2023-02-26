package com.example.demopruebasubirpdf.commons;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.example.demopruebasubirpdf.model.Response;

@ControllerAdvice
public class FileUploadExceptionAdvice {
	
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Response> handleMaxSizeException(MaxUploadSizeExceededException ex){
		System.out.print("\nmensaje de error 1\n"+ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Verifica el tamaño de los archivos "+ ex));
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Response> handleException(Exception ex){
		System.out.print("\nmensaje de error 2\n"+ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Verifica el tamaño de los archivos "+ex ));
	}
}
