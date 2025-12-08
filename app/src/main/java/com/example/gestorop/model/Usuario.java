package com.example.gestorop.model;

public class Usuario {
    private String id;
    private String nombre;
    private String email;

    private Rol rol; // "ADMIN", "SUPERVISOR", "RESIDENTE"

    public Usuario() {
        // Constructor vacío requerido obligatoriamente por Firebase
    }

    public Usuario(String uid, String nombre, String email, Rol rol) {
        this.id = uid;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Rol  getRol() { return rol; }
    public void setRol(Rol  rol) { this.rol = rol; }
}