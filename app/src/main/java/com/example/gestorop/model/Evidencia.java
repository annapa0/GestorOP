package com.example.gestorop.model;


import java.time.LocalDateTime;


public class Evidencia {

    private Integer id;
    private Integer obraId;
    private Integer usuarioId;
    private String tipo; //foto video
    private String latitud;
    private String longitud;
    private String ubicacion;
    private String descripcion;
    private LocalDateTime fechaHora;
    private boolean aprovado;

    public Evidencia(Integer id, Integer obraId, Integer usuarioId, String tipo, String latitud, String longitud, String ubicacion, String descripcion, LocalDateTime fechaHora, boolean aprovado) {
        this.id = id;
        this.obraId = obraId;
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.latitud = latitud;
        this.longitud = longitud;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.aprovado = aprovado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getObraId() {
        return obraId;
    }

    public void setObraId(Integer obraId) {
        this.obraId = obraId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public boolean isAprovado() {
        return aprovado;
    }

    public void setAprovado(boolean aprovado) {
        this.aprovado = aprovado;
    }
}
