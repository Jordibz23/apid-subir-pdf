package com.example.demopruebasubirpdf.model;

import java.util.Date;

public class File {
	private String name;
	private String url;
	private String fechaCreacion;
	private String fechaModificacion;
	private String ruta;
	
	public File(String name, String url, String fechaCreacion, String fechaModificacion, String ruta) {
		super();
		this.name = name;
		this.url = url;
		this.fechaCreacion = fechaCreacion;
		this.fechaModificacion = fechaModificacion;
		this.ruta =ruta;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion=fechaCreacion;
	}
	public String getFechaCreacion() {
		return fechaCreacion;
	}
	public void setFechaModificacion(String fechaModificacion) {
		this.fechaModificacion=fechaModificacion;
	}
	public String getFechaModificacion() {
		return fechaModificacion;
	}
	public void setRuta(String ruta){this.ruta =ruta;}
	public String getRuta(){return ruta;}
}
