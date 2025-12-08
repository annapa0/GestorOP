package com.example.gestorop.model;

import java.util.Date;

public class Obra {
    // CAMBIO 1: IDs de Integer a String para compatibilidad con Firebase
    private String id;
    private String nombre;
    private String estatus; // iniciando, proceso, terminando
    private String latitud;
    private String longitud;
    private String ubicacion;
    private String usuarioId; // ID del supervisor (String UID de Firebase)
    private Date fechaInicio;
    private Date fechaFin;

    // CAMBIO 2: Constructor Vacío OBLIGATORIO para Firebase
    public Obra() {
    }

    // Constructor con todos los datos
    public Obra(String id, String nombre, String estatus, String latitud, String longitud, String ubicacion, String supervisorAsignado, Date fechaInicio, Date fechaFin) {
        this.id = id;
        this.nombre = nombre;
        this.estatus = estatus;
        this.latitud = latitud;
        this.longitud = longitud;
        this.ubicacion = ubicacion;
        this.usuarioId = supervisorAsignado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
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

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }
}