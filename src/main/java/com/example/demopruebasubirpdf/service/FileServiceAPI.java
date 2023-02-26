package com.example.demopruebasubirpdf.service;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileServiceAPI {
	public void save(MultipartFile file, String carpeta) throws Exception;
	public Resource load(String name) throws Exception;
	public void save(List<MultipartFile> files,String nombre) throws Exception;
	public Stream<Path> loadAll(String carpeta) throws Exception;
	public void nombreFolder(String carpeta) throws Exception;
}
