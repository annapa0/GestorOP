package com.example.gestorop.model;

import java.util.Date;

public class Obra {
    private String id;
    private String nombre;
    private String estatus;
    private String latitud;
    private String longitud;
    private String ubicacion;
    private String supervisorId;
    private Date fechaInicio;
    private Date fechaFin;

    // 1. CONSTRUCTOR VACÍO (OBLIGATORIO)
    public Obra() {
    }

    // 2. CONSTRUCTOR COMPLETO
    public Obra(String id, String nombre, String estatus, String latitud, String longitud, String ubicacion, String supervisorId, Date fechaInicio, Date fechaFin) {
        this.id = id;
        this.nombre = nombre;
        this.estatus = estatus;
        this.latitud = latitud;
        this.longitud = longitud;
        this.ubicacion = ubicacion;
        this.supervisorId = supervisorId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // 3. GETTERS Y SETTERS
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public String getLatitud() { return latitud; }
    public void setLatitud(String latitud) { this.latitud = latitud; }

    public String getLongitud() { return longitud; }
    public void setLongitud(String longitud) { this.longitud = longitud; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }
}