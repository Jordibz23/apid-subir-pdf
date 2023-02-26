package com.example.demopruebasubirpdf.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.example.demopruebasubirpdf.model.File;
import com.example.demopruebasubirpdf.model.Response;
import com.example.demopruebasubirpdf.service.FileServiceAPI;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
public class FileController {

	@Autowired
	private FileServiceAPI fileServiceAPI;

	@PostMapping("/upload")
	public ResponseEntity<Response> uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("carpeta") String nombre) throws Exception {
		crearcarpeta(nombre);
		fileServiceAPI.save(files, nombre);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new Response("Los archivos fueron cargados correctamente al servidor"));
	}
	
	@GetMapping("/{filename:.+}")
	public ResponseEntity<Resource> getFile(@PathVariable String filename) throws Exception{
		Resource resource = fileServiceAPI.load(filename);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+resource.getFilename()+"\"").body(resource);
	}
	
	@GetMapping("/ver/{filename:.+}")
	public void getPdf(HttpServletResponse response,@PathVariable String filename, String lugar) throws IOException {
		response.setContentType("application/pdf");
		String rutacarpeta= ruta(lugar);
		String rutatotal=rutacarpeta+"\\"+filename;
		InputStream inputStream = new FileInputStream(rutatotal);
        OutputStream outputStream = response.getOutputStream();
        int bytesRead;
        byte[] buffer = new byte[4096];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
    }
	
	@GetMapping("/eliminar/{filename:.+}")
	public ResponseEntity<Response> deletePdf(HttpServletResponse response,@PathVariable String filename, String lugar) throws IOException {
		String rutacarpeta= ruta(lugar);
		String rutatotal=rutacarpeta+"\\"+filename;;
		java.io.File archivo = new java.io.File(rutatotal);
		if (archivo.exists()) {
			FileSystemUtils.deleteRecursively(archivo);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response("Los archivos fueron eliminados correctamente al servidor"));
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new Response("Ocurrio un error"));
		}
    }
	
	
	@GetMapping("/all")
	public ResponseEntity<List<File>> getAllFiles(@RequestParam("carpeta") String nombre) throws Exception{
		List<File> files = fileServiceAPI.loadAll(nombre).map(path-> {
			String filename = path.getFileName().toString();
			String encodedFilename = UriUtils.encodePath(filename, StandardCharsets.UTF_8);
			String url = MvcUriComponentsBuilder.fromMethodName(FileController.class, "getFile", encodedFilename).build().toString();
			//String url = MvcUriComponentsBuilder.fromMethodName(FileController.class, "getFile", path.getFileName().toString()).build().toString();
			return new File(filename, url);
		}).collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(files);
	}
	
	public static void crearcarpeta(String carpe) {
        String rutacarpeta = ruta(carpe);
        java.io.File folder = new java.io.File(rutacarpeta);
        if (!folder.exists()) {
            boolean result = folder.mkdirs();
            if (result) {
                System.out.println("Folder created successfully.");
            } else {
                System.out.println("Folder creation failed.");
            }
        } else {
            System.out.println("Folder already exists.");
        }
    }
	
	public static String ruta(String foldd) {
		Path rootFolder = Paths.get(foldd);
		Path absolutePath = rootFolder.toAbsolutePath();
		return absolutePath.toString();
	}
}
