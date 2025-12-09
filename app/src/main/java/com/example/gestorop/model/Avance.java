package com.example.gestorop.model;

import java.util.Date;

public class Avance {

    // CAMPOS DE DATOS (Deben coincidir con Firebase)
    private String id;
    private String obraId;
    private String usuarioId;
    private String usuarioEmail;
    private String etapa;
    private String descripcion;
    private String fotoUrl;
    private String videoUrl;
    private String tipo;
    private String estado;
    private Date fechaRegistro;

    // ¡ESTOS FALTABAN! Son necesarios para evitar el error del log
    private double latitud;
    private double longitud;

    // CONSTRUCTOR VACÍO (Obligatorio para Firebase)
    public Avance() {}

    // ==========================================
    // GETTERS Y SETTERS (Obligatorios)
    // ==========================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getObraId() { return obraId; }
    public void setObraId(String obraId) { this.obraId = obraId; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public String getEtapa() { return etapa; }
    public void setEtapa(String etapa) { this.etapa = etapa; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
}