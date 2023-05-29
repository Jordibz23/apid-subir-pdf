package com.example.demopruebasubirpdf.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileServiceAPI{

	private Path rootFolder = Paths.get("uploads");

	@Override
	public void nombreFolder(String carpeta) throws Exception {
		if (!carpeta.isEmpty()) {
			rootFolder = Paths.get(carpeta);
		}
	}

	@Override
	public void save(MultipartFile file,String carpeta) throws Exception {
		nombreFolder(carpeta);
        /*for (int i = 0; i < 10; i++) {
			Path absolutePath = rootFolder.toAbsolutePath();
			System.out.print(absolutePath.toString()+"\n mensaje");
		}*/
		System.out.println("Nombre de ruta "+this.rootFolder.resolve(file.getOriginalFilename()));
		Files.copy(file.getInputStream(), this.rootFolder.resolve(file.getOriginalFilename()));
	}

	@Override
	public Resource load(String name) throws Exception {
		Path file = rootFolder.resolve(name);
		Resource resource =  new UrlResource(file.toUri());
		return resource;
	}

	@Override
	public void save(List<MultipartFile> files,String carpeta) throws Exception {
		for (MultipartFile file : files) {
			this.save(file,carpeta);
		}
	}

	@Override
	public Stream<Path> loadAll(String nombre) throws Exception {
		nombreFolder(nombre);
		return Files.walk(rootFolder, 1).filter(path -> !path.equals(rootFolder)).map(rootFolder::relativize);
	}

	@Override
	public void crearCarpetaPorDocente(String carpeta) throws Exception {
		// TODO Auto-generated method stub
	}


}
