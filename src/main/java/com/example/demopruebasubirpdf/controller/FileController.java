package com.example.demopruebasubirpdf.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
		RequestMethod.DELETE })
public class FileController {

	@Autowired
	private FileServiceAPI fileServiceAPI;

	private String lugarVerPdf="";

	static String[] nombresCarpetas = {"FILTRACION IDENTIFICACION PERSONAL", "ESTUDIOS E INSTRUCCIONES", "CONTRATOS",
			"RENUNCIA Y LIQUIDACION", "DESPLAZAMIETNO PERSONAL", "DESTAQUES REASIGNACION", "DESCANSO MEDICO",
			"PERMISOS LICENCIAS Y VACACIONES", "BONIFICACION PERSONAL", "BONIFICACION FAMILIAR", "EXPERIENCIA LABORAL",
			"EVALUACIONES", "MERITOS", "DEMERITOS", "PRODUCCION INTELECTUAL Y CULTURAL", "OTROS" };

	static String[] nombresFacultades = {"vacio", "BROMATOLOGIA Y NUTRICION", "CIENCIAS", "CIENCIAS ECONOMICAS, CONTABLES Y FINANCIERAS",
			"CIENCIAS EMPRESARIALES", "CIENCIAS SOCIALES", "DERECHO Y CIENCIAS POLITICAS", "EDUCACION",
			"INGENIERIA AGRARIA, INDUSTRIAS ALIMENTARIAS Y AMBIENTAL", "INGENIERIA CIVIL", "INGENIERIA INDUSTRIAL, SISTEMAS E INFORMATICA",
			"INGENIERIA PESQUERA", "INGENIERIA QUIMICA Y METALURGICA", "MEDICINA HUMANA"};

	private String newFiles = "No hay archivos nuevos";

	@PostMapping("/upload")
	public ResponseEntity<Response> uploadFiles(@RequestParam("files") List<MultipartFile> files,
												@RequestParam("carpeta") String nombre) throws Exception {
		newFiles = "nuevo archivo";
		fileServiceAPI.save(files, nombre);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new Response("Los archivos fueron cargados correctamente al servidor"));
	}

	@PostMapping("crearCarpetas")
	public ResponseEntity<Response> crearCarpetas(@RequestParam("facultad")int idFacultad,@RequestParam("usuario")String usu) throws Exception{
		try {
			crearcarpeta("escalafon\\"+nombresFacultades[idFacultad]+"\\"+usu);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response("Se crearon las carpetas"));
		}catch (Exception e){
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new Response("Ocurrió un error al crear las carpetas"));
		}
	}

	@GetMapping("/{filename:.+}")
	public ResponseEntity<Resource> getFile(@PathVariable String filename) throws Exception {
		Resource resource = fileServiceAPI.load(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	@GetMapping("/ver/{filename:.+}")
	public void getPdf(HttpServletResponse response, @PathVariable String filename) throws IOException {
		response.setContentType("application/pdf");
		String rutacarpeta = ruta(lugarVerPdf);
		String rutatotal = rutacarpeta + "/" + filename;
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
	public ResponseEntity<Response> deletePdf(HttpServletResponse response, @PathVariable String filename, String lugar)
            throws IOException {
		String rutacarpeta = ruta(lugar);
		String rutatotal = rutacarpeta + "/" + filename;
		;
		java.io.File archivo = new java.io.File(rutatotal);
		if (archivo.exists()) {
			FileSystemUtils.deleteRecursively(archivo);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response("Los archivos fueron eliminados correctamente al servidor"));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Ocurrio un error"));
		}
	}

	@GetMapping("/mensaje")
	public ResponseEntity<Response> newFile(HttpServletResponse response) throws IOException {
		System.out.print(newFiles);
		if (newFiles.equalsIgnoreCase("nuevo archivo")) {
			newFiles = "No hay archivos nuevos";
			return ResponseEntity.status(HttpStatus.OK).body(new Response("Se subio un nuevo archivo"));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response("Sin archivos nuevos"));
		}

	}

	@GetMapping("/all")
	public ResponseEntity<List<File>> getAllFiles(@RequestParam("carpeta") String nombre,@RequestParam("facultad") int idFacultad) throws Exception {
		String rutaCarpetaUsuario="escalafon\\"+nombresFacultades[idFacultad]+"\\"+nombre;
		lugarVerPdf=rutaCarpetaUsuario;
		List<File> files = fileServiceAPI.loadAll(rutaCarpetaUsuario).map(path -> {
			String filename = path.getFileName().toString();
			String encodedFilename = UriUtils.encodePath(filename, StandardCharsets.UTF_8);
			String url = MvcUriComponentsBuilder.fromMethodName(FileController.class, "getFile", encodedFilename)
					.build().toString();
			String rutaArchivo =rutaCarpetaUsuario+"\\"+filename;
			String[] fechas=obtenerFecha(rutaArchivo);
			return new File(filename, url,fechas[0],fechas[1],rutaArchivo);
		}).collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(files);
	}

	public static void crearcarpeta(String carpe) {
		String rutacarpeta = ruta(carpe);
		java.io.File folder = new java.io.File(rutacarpeta);
		if (!folder.exists()) {
			boolean result = folder.mkdirs();
			if (result) {
				for (int i = 0; i < nombresCarpetas.length; i++) {
					java.io.File subfolders = new java.io.File(rutacarpeta+"\\"+nombresCarpetas[i]);
					if (!subfolders.exists()) {
						boolean resulta = subfolders.mkdirs();
						if (resulta) {
							System.out.println("Folder "+nombresCarpetas[i]+" created successfully.");
						}else {
							System.out.println("Folder creation failed.");
						}
					}
				}
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

	private String[] obtenerFecha(String rutaArchivo) {
		try {
			Path path2 = Path.of(rutaArchivo);
			BasicFileAttributes attributes = Files.readAttributes(path2, BasicFileAttributes.class);
			FileTime creationTime = attributes.creationTime();
			FileTime lastModifiedTime = attributes.lastModifiedTime();
			Date ModifiedDate = new Date(lastModifiedTime.toMillis());
			Date creationDate = new Date(creationTime.toMillis());
			// Formatear la fecha con la zona horaria GMT-5
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT-5"));
			String[] formattedDate = {sdf.format(creationDate),sdf.format(ModifiedDate)};
			return formattedDate;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
